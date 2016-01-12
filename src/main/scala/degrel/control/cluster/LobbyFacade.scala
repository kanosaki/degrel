package degrel.control.cluster

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import degrel.cluster.messages.{LobbyState, QueryStatus, TellLobby}
import degrel.cluster.{MemberBase, Timeouts, Roles, Lobby}

import scala.concurrent.Future

class LobbyFacade(system: ActorSystem) {
  implicit val timeout = Timeouts.short

  import system.dispatcher

  var current: ActorRef = null

  def isActive: Future[Boolean] = {
    (current ? QueryStatus()) map {
      case LobbyState(active) => active
    }
  }

  def start() = {
    current = system.actorOf(Props[Lobby], name = Roles.Lobby.name)
    current ! TellLobby(current.path.address)
  }

  def connect(addr: Address): Future[Unit] = {
    val path = MemberBase.actorPath(addr, Roles.Lobby)
    this.connect(path)
  }

  def connect(path: ActorPath)(implicit timeout: Timeout): Future[Unit] = {
    if (current != null) {
      return Future {}
    }
    system.actorSelection(path).resolveOne() map { ref =>
      current = ref
    }
  }

  def address: String = {
    current match {
      case null => throw new RuntimeException("Non initialized Lobby")
      case _ => {
        current.path.toStringWithAddress(current.path.address)
      }
    }
  }
}

object LobbyFacade {
  def apply(sys: ActorSystem) = {
    new LobbyFacade(sys)
  }
}
