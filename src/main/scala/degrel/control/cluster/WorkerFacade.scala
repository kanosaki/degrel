package degrel.control.cluster

import akka.actor.{Address, Props, ActorRef, ActorSystem}
import degrel.cluster.{Roles, Worker}
import degrel.cluster.messages.TellLobby

class WorkerFacade(system: ActorSystem, seed: Address) {
  var current: ActorRef = null

  def start() = {
    current = system.actorOf(Props[Worker], name = Roles.Worker.name)
    current ! TellLobby(seed)
  }
}

object WorkerFacade {
  def apply(sys: ActorSystem, seed: Address) = {
    new WorkerFacade(sys, seed)
  }
}
