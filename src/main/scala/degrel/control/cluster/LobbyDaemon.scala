package degrel.control.cluster

import akka.actor.{Props, ActorSystem, ActorRef}
import degrel.cluster.messages.TellLobby
import degrel.cluster.{Roles, Lobby}

class LobbyDaemon(system: ActorSystem) {
  var current: ActorRef = null

  def start() = {
    current = system.actorOf(Props[Lobby], name = Roles.Lobby.name)
    current ! TellLobby(current.path.address)
  }
}

object LobbyDaemon {
  def apply(sys: ActorSystem) = {
    new LobbyDaemon(sys)
  }
}
