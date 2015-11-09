package degrel.cluster

import akka.pattern.ask
import akka.util.Timeout
import degrel.core.{Cell, Vertex}

import scala.concurrent.duration._

// Akka controller
class Controller() extends MemberBase {
  val node = LocalNode(context.system)

  import Controller.messages._
  import context.dispatcher
  import messages._

  override def receiveMsg = {
    case Interpret(cell) if lobbies.isEmpty => {
      log.error("*********** No islands!")
    }
    case Interpret(cell) if lobbies.nonEmpty => {
      log.info(s"************* Engine Start: ${cell.pp}")
      val rootIsland = lobbies.head
      implicit val timeout = Timeout(10.hours)
      val packed = node.exchanger.packAll(cell)
      val origin = sender()
      rootIsland ? Push(packed) onSuccess {
        case Fin(v) =>
          val unpacked = node.exchanger.unpack(v)
          log.info(s"************* Engine result: ${unpacked.pp}")
          origin ! Result(unpacked)
      }
    }
  }
}

object Controller {
  val DEFAULT_ENGINE_NAME = "degrel"

  object messages {

    case class Interpret(cell: Cell)

    case class Result(v: Vertex)

  }

}

