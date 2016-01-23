package degrel.control.cluster

import akka.actor.{ActorSystem, Address}
import akka.pattern.ask
import degrel.cluster.messages.{ControllerState, Halt, QueryStatus}
import degrel.cluster.{Controller, Timeouts}
import degrel.core.{Cell, Vertex}

import scala.async.Async.{async, await}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

// Adapter class for degrel cluster controller
class ControllerFacade(val system: ActorSystem, lobbyAddr: Address) {

  import Controller.messages._
  import system.dispatcher

  val ctrlr = system.actorOf(Controller.props(lobbyAddr))

  val lobby = LobbyFacade(system)

  def isActive: Future[Boolean] = async {
    implicit val timeout = Timeouts.short
    await(ctrlr ? QueryStatus()) match {
      case ControllerState(active) => active
    }
  }

  def interpret(cell: Cell): Future[Vertex] = async {
    implicit val timeout = Timeouts.long
    val res = await(ctrlr ? Interpret(cell)) match {
      case Result(v) => v
    }
    Await.ready(ctrlr ? Halt, 5.seconds)
    await(system.terminate())
    System.exit(0)
    res
  }

  def isReady: Future[Boolean] = {
    for {
      _ <- lobby.connect(lobbyAddr)
      lret <- lobby.isActive
      cret <- this.isActive

    } yield lret && cret
  }

  def waitForReady(): Unit = {
    val res = Await.result(this.isReady, Timeouts.short.duration + 5.seconds)
    if (!res) {
      Thread.sleep(100)
      waitForReady()
    }
  }
}

object ControllerFacade {
  def apply(sys: ActorSystem, lobby: Address) = {
    new ControllerFacade(sys, lobby)
  }
}
