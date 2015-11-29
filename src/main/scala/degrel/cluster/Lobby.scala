package degrel.cluster

import akka.actor.ActorRef
import akka.pattern.ask

import scala.collection.mutable
import scala.concurrent.Future


/**
  * Host(Ocean, Island, Controller)とSessionを管理します
  * Controllerからの依頼を受けて，適切なIslandからNodeを割り当てたりします
  * また，リソースの管理等も行います
  * TODO: 複数のOceanがいたときに，協調して動作するように
  */
class Lobby extends MemberBase {
  val sessions = mutable.ListBuffer[ActorRef]()

  // mapping: Worker -> SessionManager
  val allocationMapping = mutable.HashMap[ActorRef, ActorRef]()

  import context.dispatcher
  import messages._

  private def allocateNode(manager: ActorRef, param: NodeInitializeParam): Future[Option[ActorRef]] = {
    implicit val timeout = Timeouts.short
    workers.find(w => !allocationMapping.contains(w)) match {
      case Some(worker) => {
        (worker ? SpawnNode(manager, param)) map {
          case Right(node: ActorRef) => {
            allocationMapping += worker -> manager
            Some(node)
          }
        }
      }
      case None => {
        Future {
          None
        }
      }
    }
  }

  private def allocateSession(): ActorRef = {
    val session = context.actorOf(SessionManager.props(self))
    sessions += session
    session
  }

  override def receiveMsg: Receive = {
    case QueryStatus() => {
      sender() ! LobbyState(active = this.workers.nonEmpty)
    }
    case NewSession() => {
      val newSession = allocateSession()
      println(s"================= New Session: $newSession")
      sender() ! Right(newSession)
    }

    case CloseSession(sess) => {
      sessions -= sess
      context.stop(sess)
    }
    case NodeAllocateRequest(manager, param) => {
      println(s"================= Allocating node for: $manager param: $param")
      val origin = sender()
      allocateNode(manager, param).onSuccess {
        case Some(node) => {
          origin ! Right(node)
        }
        case None => {
          origin ! Left(new RuntimeException("No free worker!"))
        }
      }
    }
  }

  override def role: MemberRole = Roles.Lobby
}
