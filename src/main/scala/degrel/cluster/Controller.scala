package degrel.cluster

import akka.actor.{Props, Address, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import degrel.cluster.journal.{JournalPrinter, JournalPayload}
import degrel.core.{Cell, Vertex}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.async.Async.{async, await}

// Akka controller
class Controller(lobbyAddr: Address) extends MemberBase {
  val node = LocalNode(context.system)

  import Controller.messages._
  import context.dispatcher
  import messages._
  this.joinLobby(lobbyAddr)

  def finalizeSession(sess: ActorRef, lobby: ActorRef): Future[Unit] = async {
    implicit val timeout = Timeouts.infoGather
    println("=============== Finalizing")
    val journalRes = await(sess ? FetchJournal(streamReq = false))
    journalRes match {
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
    case Interpret(cell) => {
      val origin = sender()
      async {
        Await.result(initialLobby.future, 5.seconds) // Wait for cluster system  (This operation is essential)
        implicit val timeout = Timeout(10.hours)
        log.info(s"************* Engine Start: ${cell.pp}")
        val lobby = lobbies.head
        val packed = node.exchanger.packAll(cell)
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

  override def role: MemberRole = Roles.Controller
}

object Controller {
  def props(lobbyAddr: Address) = Props(new Controller(lobbyAddr))
  val DEFAULT_ENGINE_NAME = "degrel"

  object messages {

    case class Interpret(cell: Cell)

    case class Result(v: Vertex)

  }

}

