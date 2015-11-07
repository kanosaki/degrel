package degrel.control.cluster

import akka.actor.{Props, ActorRef, ActorSystem}
import degrel.cluster.Island

class IslandFacade(system: ActorSystem) {
  var current: ActorRef = null
  def start() = {
    current = system.actorOf(Props[Island])
  }
}
