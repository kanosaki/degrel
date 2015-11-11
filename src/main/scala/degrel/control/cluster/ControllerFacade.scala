package degrel.control.cluster

import akka.actor.{Address, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import degrel.cluster.messages.{ControllerState, LobbyState, QueryStatus, TellLobby}
import degrel.cluster.{Timeouts, Roles, Controller, Controller$}
import degrel.core.{Cell, Vertex}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

// Adapter class for degrel cluster controller
class ControllerFacade(val system: ActorSystem, lobbyAddr: Address) {
  implicit val timeout = Timeouts.short

  import system.dispatcher
  import Controller.messages._

  val ctrlr = system.actorOf(Props[Controller], name = Roles.Controller.name)
  ctrlr ! TellLobby(lobbyAddr)

  val lobby = LobbyFacade(system)

  def isActive: Future[Boolean] = {
    (ctrlr ? QueryStatus()) map {
      case ControllerState(active) => active
    }
  }

  def interpret(cell: Cell): Future[Vertex] = {
    (ctrlr ? Interpret(cell)).map {
      case Result(v) => v
    }
  }

  def isReady: Future[Boolean] = {
    for {
      _ <- lobby.connect(lobbyAddr)
      lret <- lobby.isActive
      cret <- this.isActive

    } yield lret && cret
  }
}

object ControllerFacade {
  def apply(sys: ActorSystem, lobby: Address) = {
    new ControllerFacade(sys, lobby)
  }
}
