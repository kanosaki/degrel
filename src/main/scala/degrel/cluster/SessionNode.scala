package degrel.cluster

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import degrel.core.VertexPin
import degrel.engine.rewriting.Binding
import degrel.engine.{Driver, RemoteDriver}

import scala.async.Async.{async, await}
import scala.concurrent.Future

class SessionNode(baseIsland: ActorRef, manager: ActorRef, param: NodeInitializeParam) extends ActorBase {

  val repo = RemoteRepository(manager)
  val journal = JournalAdapter(manager, self, param.id)
  val localNode = LocalNode(context.system, journal, repo)
  localNode.selfID = param.id

  def driverFactory = localNode.driverFactory

  def chassis = localNode.chassis

  import context.dispatcher
  import messages._

  def updateNeighbors(): Future[Unit] = async {
    implicit val timeout = Timeouts.short
    localNode.registerNode(1, manager)
    await(manager ? QueryStatus()) match {
      case SessionState(nodes) => {
        nodes.foreach { case (id, ref) =>
          if (ref != self) {
            localNode.registerNode(id, ref)
          }
        }
      }
    }
  }


  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    this.updateNeighbors()
  }

  def spawnDriver(dCell: DGraph, binding: Binding, returnTo: VertexPin, parent: Driver) = {
    val unpacked = localNode.exchanger.unpack(dCell)
    if (unpacked.isCell) {
      localNode.spawnLocally(unpacked, binding, returnTo, parent)
    } else {
      throw new RuntimeException("Only cells can spawn on driver.")
    }
  }

  override def receiveBody = {
    case QueryStatus() => {
      sender() ! NodeState(localNode.selfID, manager)
    }
    case SessionState(nodes) => {
      nodes.foreach { case (id, ref) =>
        if (ref != self) {
          localNode.registerNode(id, ref)
        }
      }
    }
    case QueryGraph(id, options) => {
      val origin = sender()
      localNode.lookupOwnerLocal(id) match {
        case Right(drv) => {
          drv.getVertex(id) match {
            case Some(v) => {
              val dGraph = localNode.exchanger.packForQuery(v, options)
              origin ! Right(dGraph)
            }
            case None => origin ! Left(new RuntimeException(s"Graph not found for $id in $drv (${drv.id})"))
          }
        }
        case Left(msg) => {
          println("Cannot find owner! please select proper node.")
          origin ! Left(msg)
        }
      }
    }
    case SendGraph(target, graph) => {
      if (chassis.verbose) {
        println(s"SendGraph on: ${localNode.selfID} $target $graph")
      }
      localNode.lookupOwner(target) map {
        case Right(drv) => {
        }
        case Left(err) => {
          sender() ! "Cannto send data"
        }
      }
    }
    case LookupDriver(id) => {
      if (chassis.verbose) {
        println(s"LookupDriver on ${localNode.selfID} $id")
      }
      val origin = sender()
      localNode.lookupOwnerLocal(id) match {
        case Right(drv) => origin ! Right(drv.param(self))
        case Left(msg) => origin ! Left(msg)
      }
    }
    case SpawnDriver(graph, binding, returnTo) => {
      if (chassis.verbose) {
        println(s"SpawnDriver on: ${localNode.selfID}")
        println(graph.pp)
      }
      val origin = sender()
      async {
        await(localNode.lookupOwner(returnTo.id)) match {
          case Right(parent) => {
            val driver = this.spawnDriver(graph, Binding.empty(), returnTo, parent)
            driver.start()
            origin ! Right(driver.param(self))
          }
          case Left(msg) => {
            msg.printStackTrace()
            log.error(s"Unknown return VertexPin ${msg.getMessage}")
            origin ! Left(msg)
          }
        }
      }
    }
    case TellDriverInfo(info: DriverInfo) => {
      if (chassis.verbose) {
        println(s"TellDriverInfo $info")
      }
      // Remote driver state is updated.
      localNode.lookupOwnerLocal(info.origin) match {
        case Right(drv: RemoteDriver) => {
          drv.remoteUpdated(info)
        }
        case _ => {
          log.warning(s"Cannot update info $info")
        }
      }
    }
    case Run(msg) => {
      if (chassis.verbose) {
        println("Running:")
        println(msg.pp)
      }
      val origin = sender()
      this.updateNeighbors() map { _ =>
        val driver = this.spawnDriver(msg, Binding.empty(), null, null)
        driver.start()
        async {
          val result = await(driver.finValue.future)
          val packed = localNode.exchanger.packAll(result)
          origin ! messages.Fin(packed)
        }
      }
    }
  }
}

object SessionNode {
  def props(baseIsland: ActorRef, manager: ActorRef, param: NodeInitializeParam) = Props(new SessionNode(baseIsland, manager, param))
}

