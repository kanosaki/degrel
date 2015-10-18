package degrel.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import degrel.core.{Vertex, Cell}

import scala.collection.mutable
import scala.concurrent.duration._

// Akka controller
class Engine() extends Actor with ActorLogging {
  val islands = mutable.ListBuffer[ActorRef]()

  import messages._
  import context.dispatcher
  import Engine.messages._

  override def receive = {
    case IslandRegistration if !islands.contains(sender()) => {
      context.watch(sender())
      islands += sender()
    }
    case Rewrite(cell) if islands.isEmpty => {
      log.error("No islands!")
    }
    case Rewrite(cell) if islands.nonEmpty => {
      val rootIsland = islands.head
      implicit val timeout = Timeout(10.hours)
      rootIsland ? Push(cell) onSuccess {
        case Fin(v) =>
          sender() ! Result(v)
      }
    }
  }
}

object Engine {
  val DEFAULT_ENGINE_NAME = "degrel"

  object messages {

    case class Rewrite(cell: Cell)

    case class Result(v: Vertex)

  }

}

