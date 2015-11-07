package degrel.control.cluster

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import degrel.cluster.{Controller, Controller$}
import degrel.core.{Cell, Vertex}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

// Adapter class for degrel cluster controller
class ControllerFacade(val system: ActorSystem) {
  val engine = system.actorOf(Props[Controller])

  import Controller.messages._

  def rewrite(cell: Cell): Future[Vertex] = {
    implicit val timeout = Timeout(10.hours)
    (engine ? Interpret(cell)).mapTo[Vertex]
  }
}
