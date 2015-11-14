package degrel.cluster

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import degrel.core.{Cell, Vertex}

import scala.concurrent.Future
import scala.concurrent.duration._

// Akka controller
class Controller() extends MemberBase {
  val node = LocalNode(context.system)

  import Controller.messages._
  import context.dispatcher
  import messages._

  def finalizeSession(sess: ActorRef, lobby: ActorRef): Future[Unit] = {
    implicit val timeout = Timeouts.infoGather
    println("=============== Finalizing")
    (sess ? FetchJournal(false)) map {
      case Right(jps: Vector[JournalPayload]) => {
        println(jps)
        JournalPrinter(jps).printTo(System.out)
        lobby ! CloseSession(sess)
      }
      case v => {
        println(s"************************************************** $v")
        throw new RuntimeException("Invalid match")
      }
    }
  }

  override def receiveMsg = {
    case QueryStatus() => {
      sender() ! ControllerState(active = lobbies.nonEmpty)
    }
    case Interpret(cell) if lobbies.isEmpty => {
      log.error("*********** No Lobby!!")
    }
    case Interpret(cell) if lobbies.nonEmpty => {
      implicit val timeout = Timeout(10.hours)
      log.info(s"************* Engine Start: ${cell.pp}")
      val lobby = lobbies.head
      val packed = node.exchanger.packAll(cell)
      val origin = sender()
      for {
        session <- (lobby ? NewSession()) map {
          case Right(ref: ActorRef) => ref
          case Left(msg: Throwable) => {
            log.error(msg, "Cannot allocate session")
            throw msg
          }
        }
        result <- session ? StartInterpret(packed, self)
      } yield {
        result match {
          case Fin(v) =>
            val unpacked = node.exchanger.unpack(v)
            origin ! Result(unpacked)
            log.info(s"************* Engine result: ${unpacked.pp}")
            finalizeSession(session, lobby)
        }
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

