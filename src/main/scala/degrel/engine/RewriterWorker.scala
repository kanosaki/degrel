package degrel.engine

import akka.actor.{Actor, Props}
import degrel.engine.rewriting.{Reserve, Rewriter}

object RewriterWorker {
  def apply(rewriter: Rewriter) = {
    system.actorOf(this.props(rewriter))
  }

  def props(rewriter: Rewriter): Props = Props(classOf[RewriterWorker], rewriter)

  case class Step(reserve: Reserve)

  case class Result(rewrote: Boolean)

}

class RewriterWorker(rewriter: Rewriter) extends Actor {


  def receive: Actor.Receive = {
    case RewriterWorker.Step(reserve) => // sender ! RewriterWorker.Result(rewriter.step(reserve))
  }
}
