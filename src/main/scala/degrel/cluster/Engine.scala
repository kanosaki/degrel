package degrel.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import degrel.core.{Cell, Vertex}

import scala.collection.mutable
import scala.concurrent.duration._

// Akka controller
class Engine() extends Actor with ActorLogging {
  val islands = mutable.ListBuffer[ActorRef]()
  val node = new LocalNode(context.system.name, context.system.settings.config)

  import Engine.messages._
  import context.dispatcher
  import messages._

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
      val packed = node.exchanger.packAll(cell)
      val origin = sender()
      rootIsland ? Push(packed) onSuccess {
        case Fin(v) =>
          val unpacked = node.exchanger.unpack(v)
          log.info("-----------------------------------------")
          log.info(s"Engine receive: ${unpacked.pp}")
          log.info("-----------------------------------------")
          origin ! Result(unpacked)
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

