package degrel.control

import scala.io.Source
import degrel.rewriting.LocalReserve
import degrel.front.ParserUtils
import degrel.core.{Vertex, Rule}
import degrel.engine.RewriteScheduler
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask

class Interpreter(source: Source) {
  val reserve = new LocalReserve()
  private val termParser = ParserUtils
  implicit val rewriteTimeout = Timeout(10.seconds)

  def parse(s: String): Vertex = {
    termParser.parseVertex(s)
  }

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
    reserve.rewriteUntilStop()
    println("-------- RESULT --------")
    println(reserve.repr())
  }

  def rewriteMulti() = {
    val worker = RewriteScheduler(reserve)
    val future = worker ? RewriteScheduler.Run
    Await.result(future, rewriteTimeout.duration)
  }
}
