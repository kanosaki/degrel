package degrel.control.cluster

import akka.actor._
import degrel.cluster.{Timeouts, MemberBase, Roles, Worker}

import scala.concurrent.ExecutionContext
import scala.util.Success

class WorkerFacade(system: ActorSystem, seed: Address) {
  var current: ActorRef = null
  import system.dispatcher

  def start() = {
    val lobbyPath = MemberBase.actorPath(seed, Roles.Lobby)
    implicit val timeout = Timeouts.short
    system.actorSelection(lobbyPath).resolveOne().onComplete {
      case Success(lobbyRef) => {
        current = system.actorOf(Worker.props(seed, lobbyRef))
      }
      case _ => {
        throw new RuntimeException("Cannot find lobby!")
      }
    }
  }
}

object WorkerFacade {
  def apply(sys: ActorSystem, seed: Address) = {
    new WorkerFacade(sys, seed)
  }
}
