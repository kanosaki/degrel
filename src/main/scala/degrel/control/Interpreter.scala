package degrel.control

import akka.pattern.ask
import akka.util.Timeout
import degrel.core.{Rule, Vertex}
import degrel.engine.RewriteScheduler
import degrel.front.ParserUtils
import degrel.rewriting.LocalReserve

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

class Interpreter(source: Source) {
  val reserve = new LocalReserve()
  implicit val rewriteTimeout = Timeout(10.seconds)
  private val termParser = ParserUtils

  def start() = {
    for (line <- source.getLines()) {
      parse(line) match {
        case rule: Rule => {
          reserve.addRule(rule)
        }
        case v => {
          reserve.addVertex(v)
        }
      }
    }
    println("-------- INPUT ---------")
    println(reserve.repr())
    this.rewriteMulti()
    println("-------- RESULT --------")
    println(reserve.repr())
  }

  def parse(s: String): Vertex = {
    termParser.parseVertex(s)
  }

  def rewriteMulti() = {
    val worker = RewriteScheduler(reserve)
    val future = worker ? RewriteScheduler.Run
    Await.result(future, rewriteTimeout.duration)
  }
}
