package degrel.cluster

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask

import scala.concurrent.Await

class ClusterTestUtils {

}

object ClusterTestUtils {

  import messages._

  /**
    * Automatically generates a new lobby and worker, and requests
    * a new session to lobby
    * @param system ActorSystem
    * @return A future to fetch ActorRef to SessionManager
    */
  def newSession(workerNum: Int = 1)(implicit system: ActorSystem): ActorRef = {
    implicit val timeout = Timeouts.apiCall
    import system.dispatcher
    val lobby = system.actorOf(Props[Lobby])
    (0 to workerNum).foreach { i => // plus one for master node
      system.actorOf(Worker.props(lobby.path.address, lobby))
    }
    val ctrlr = system.actorOf(Controller.props(lobby.path.address))
    val sessionFuture = (lobby ? NewSession(ctrlr)) map {
      case Right(ref: ActorRef) => ref
      case _ => throw new RuntimeException("Cannot allocate session")
    }
    Await.result(sessionFuture, timeout.duration)
  }
}
