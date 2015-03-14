package degrel.control

import akka.pattern.ask
import akka.util.Timeout
import degrel.core.{Rule, Vertex}
import degrel.front.ParserUtils

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

class Interpreter(source: Source) {
  implicit val rewriteTimeout = Timeout(10.seconds)
  private[this] val termParser = ParserUtils

  def start() = {
  }

  def parse(s: String): Vertex = {
    degrel.parseVertex(s)
  }

  def rewriteMulti() = {
    //val worker = RewriteScheduler(reserve)
    //val future = worker ? RewriteScheduler.Run
    //Await.result(future, rewriteTimeout.duration)
  }
}
