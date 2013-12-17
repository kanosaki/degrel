package degrel.engine

import akka.actor.{Actor, Props}
import degrel.rewriting.{Rewriter, Reserve}

object RewriterWorker {
  def props(rewriter: Rewriter): Props = Props(classOf[RewriterWorker], rewriter)

  def apply(rewriter: Rewriter) = {
    system.actorOf(this.props(rewriter))
  }

  case class Step(reserve: Reserve)

  case class Result(rewrote: Boolean)
}

class RewriterWorker(rewriter: Rewriter) extends Actor {


  def receive: Actor.Receive = {
    case RewriterWorker.Step(reserve) => sender ! RewriterWorker.Result(rewriter.step(reserve))
  }
}
