package degrel.cluster

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import degrel.core.{Vertex, Cell}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

// Adapter class for degrel cluster controller
class ClusterController(implicit val system: ActorSystem) {
  val engine = system.actorOf(Props[Engine])
  import Engine.messages._

  def rewrite(cell: Cell): Future[Vertex] = {
    implicit val timeout = Timeout(10.hours)
    (engine ? Rewrite(cell)).mapTo[Vertex]
  }
}
