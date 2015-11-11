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

  import messages._
  import context.dispatcher

  private def allocateNode(manager: ActorRef, param: NodeInitializeParam): Future[ActorRef] = {
    implicit val timeout = Timeouts.short
    val worker = workers.head
    (worker ? SpawnNode(manager, param)) map {
      case Right(node: ActorRef) => node
    }
  }

  private def allocateSession(): ActorRef = {
    val session = context.actorOf(SessionManager.props(self), name = "session")
    sessions += session
    session
  }

  override def receiveMsg: Receive = {
    case Push(graph) => {
      println(s"ACCEPT: $graph")
      sender() ! Fin(graph)
    }
    case QueryStatus() => {
      sender() ! LobbyState(active = this.workers.nonEmpty)
    }
    case NewSession() => {
      val newSession = allocateSession()
      println(s"================= New Session: $newSession")
      sender() ! Right(newSession)
    }
    case NodeAllocateRequest(manager, param) => {
      println(s"================= Allocating node for: $manager")
      val origin = sender()
      allocateNode(manager, param).onSuccess {
        case node: ActorRef => {
          origin ! Right(node)
        }
      }
    }
  }
}
