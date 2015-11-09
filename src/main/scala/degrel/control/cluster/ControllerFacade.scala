package degrel.control.cluster

import akka.actor.{Address, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import degrel.cluster.messages.TellLobby
import degrel.cluster.{Roles, Controller, Controller$}
import degrel.core.{Cell, Vertex}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

// Adapter class for degrel cluster controller
class ControllerFacade(val system: ActorSystem, lobby: Address) {
  val engine = system.actorOf(Props[Controller], name = Roles.Controller.name)
  engine ! TellLobby(lobby)

  import Controller.messages._

  def interpret(cell: Cell): Future[Vertex] = {
    implicit val timeout = Timeout(10.hours)
    (engine ? Interpret(cell)).mapTo[Vertex]
  }
}

object ControllerFacade {
  def apply(sys: ActorSystem, lobby: Address) = {
    new ControllerFacade(sys, lobby)
  }
}
