package degrel.cluster

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.pattern.ask

import scala.concurrent.{Await, Future}

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
  def newSession(implicit system: ActorSystem): ActorRef = {
    implicit val timeout = Timeouts.apiCall
    import system.dispatcher
    val lobby = system.actorOf(Props[Lobby])
    val worker = system.actorOf(Props[Worker])
    worker ! JoinLobby(lobby)
    val sessionFuture = (lobby ? NewSession()) map {
      case Right(ref: ActorRef) => ref
      case _ => throw new RuntimeException("Cannot allocate session")
    }
    Await.result(sessionFuture, timeout.duration)
  }
}
