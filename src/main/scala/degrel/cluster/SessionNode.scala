package degrel.cluster

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import degrel.cluster.journal.JournalCollector
import degrel.core._
import degrel.engine.Driver
import degrel.engine.rewriting.Binding

import scala.async.Async.{async, await}
import scala.concurrent.{Future, stm}

class SessionNode(baseIsland: ActorRef, manager: ActorRef, param: NodeInitializeParam) extends SessionMember {

  val repo = RemoteRepository(manager)
  val journal = JournalCollector(manager, self, param.id)
  val localNode = LocalNode(context.system, journal, repo, NodeIDSpace(param.id))
  val runningDrivers = stm.Ref(0)
  val driverLimit = 1 // max running drivers

  def driverFactory = localNode.driverFactory

  def chassis = localNode.chassis

  import context.dispatcher
  import messages._

  def registerNeighbors(nodes: Seq[(NodeID, ActorRef)]) = {
    nodes.foreach { case (id, ref) =>
      if (ref != self) {
        localNode.registerNode(id, ref)
      }
    }
  }

  def updateNeighbors(): Future[Unit] = async {
    implicit val timeout = Timeouts.short
    localNode.registerNode(1, manager)
    await(manager ? QueryStatus()) match {
      case SessionState(nodes) => {
        this.registerNeighbors(nodes)
      }
    }
  }


  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    this.updateNeighbors()
  }

  def spawnDriver(unpacked: Vertex, binding: Binding, returnTo: VertexPin, parentPin: VertexPin): Future[Either[Throwable, Driver]] = async {
    if (parentPin == null) {
      Right(localNode.spawnLocally(unpacked, binding, null, null))
    } else {
      await(localNode.lookupOwner(parentPin.id)) match {
        case Right(par) => {
          Right(localNode.spawnLocally(unpacked, binding, returnTo, par))
        }
        case Left(msg) => {
          msg.printStackTrace()
          log.error(s"Unknown return VertexPin ${msg.getMessage}")
          Left(msg)
        }
      }
    }
  }

  private def spawnStart() = stm.atomic { implicit txn =>
    println(s"RUNNING DRIVERS: ${runningDrivers.get}")
    if (runningDrivers.get >= driverLimit) {
      false
    } else {
      runningDrivers += 1
      true
    }
  }


  def startDriver(origin: ActorRef, graph: DGraph, binding: Binding, returnTo: VertexPin, parent: VertexPin): Future[Either[Throwable, Vertex]] = {
    val unpacked = localNode.exchanger.unpack(graph)
    if (!unpacked.isCell) {
      return Future {
        Left(new RuntimeException("Only cells can spawn on driver."))
      }
    }
    if (this.spawnStart()) async {
      val spawnResult = await(this.spawnDriver(unpacked, binding, returnTo, parent))
      spawnResult match {
        case Right(driver: Driver) => {
          driver.start()
          origin ! Right(driver.param(self))
          val result = await(driver.finValue.future)
          runningDrivers.single -= 1
          Right(result)
        }
        case Left(msg) => {
          origin ! Left(msg)
          runningDrivers.single -= 1
          Left(msg)
        }
      }
    } else {
      log.error("Node is full! cannot spawn driver!")
      Future {
        val exc = new RuntimeException("Cannot spawn")
        origin ! Left(exc)
        Left(exc)
      }
    }
  }

  override def receiveMsg: Receive = {
    case QueryStatus() => {
      sender() ! NodeState(localNode.selfID, manager, runningDrivers.single.get)
    }
    case SessionState(nodes) => {
      this.registerNeighbors(nodes)
    }
    case SpawnDriver(graph, binding, returnTo, parent) => {
      log.debug(s"SpawnDriver on: ${localNode.selfID}")
      log.debug(graph.pp)
      val origin = sender()
      this.startDriver(origin, graph, Binding.empty(), returnTo, parent)
    }
    case Run(graph) => {
      log.debug(s"Running: ${graph.pp}")
      val origin = sender()
      async {
        await(this.updateNeighbors())
        await(this.startDriver(origin, graph, Binding.empty(), null, null)) match {
          case Right(result) => {
            log.info(s"RUNNING FINISHED: $result")
            val packed = localNode.exchanger.packAll(result)
            origin ! messages.Fin(packed)
          }
          case Left(err) => {
            log.error(err, "Running failed")
            throw err
          }
        }
      }
    }
  }
}

object SessionNode {
  def props(baseIsland: ActorRef, manager: ActorRef, param: NodeInitializeParam) = Props(new SessionNode(baseIsland, manager, param))
}

