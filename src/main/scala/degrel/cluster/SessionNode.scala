package degrel.cluster

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import degrel.core.transformer.{GraphVisitor, TransferOwnerVisitor}
import degrel.core.{NodeIDSpace, ID, Vertex, VertexPin}
import degrel.engine.Driver
import degrel.engine.rewriting.Binding

import scala.async.Async.{async, await}
import scala.concurrent.Future

class SessionNode(baseIsland: ActorRef, manager: ActorRef, param: NodeInitializeParam) extends SessionMember {

  val repo = RemoteRepository(manager)
  val journal = JournalAdapter(manager, self, param.id)
  val localNode = LocalNode(context.system, journal, repo, NodeIDSpace(param.id))

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

  def spawnDriver(unpacked: Vertex, binding: Binding, returnTo: VertexPin, parent: Driver) = {
    if (unpacked.isCell) {
      localNode.spawnLocally(unpacked, binding, returnTo, parent)
    } else {
      throw new RuntimeException("Only cells can spawn on driver.")
    }
  }

  override def receiveMsg: Receive = {
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
    case SpawnDriver(graph, binding, returnTo, parent) => {
      log.debug(s"SpawnDriver on: ${localNode.selfID}")
      log.debug(graph.pp)
      val origin = sender()
      async {
        await(localNode.lookupOwner(parent.id)) match {
          case Right(par) => {
            val unpacked = localNode.exchanger.unpack(graph)
            val driver = this.spawnDriver(unpacked, Binding.empty(), returnTo, par)
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
    case Run(msg) => {
      log.debug(s"Running: ${msg.pp}")
      val origin = sender()
      this.updateNeighbors() map { _ =>
        val unpacked = localNode.exchanger.unpack(msg)
        val driver = this.spawnDriver(unpacked, Binding.empty(), null, null)
        driver.start()
        async {
          val result = await(driver.finValue.future)
          log.info(s"RUNNING FINISHED: $result")
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

