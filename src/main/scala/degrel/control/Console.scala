package degrel.control

import degrel.front.{FrontException, ParserUtils}
import degrel.core.{Rule, Vertex}
import degrel.rewriting.LocalReserve
import scala.tools.jline.console.ConsoleReader
import degrel.engine.RewriteScheduler
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await

class Console(val reserve: LocalReserve) {
  private val termParser = ParserUtils
  private val reader: ConsoleReader = new ConsoleReader()
  val prompt = ">>> "
  implicit val rewriteTimeout = Timeout(10.seconds)

  def parse(s: String): Vertex = {
    termParser.parseVertex(s)
  }

  def nextLine(line: String) = {
    if (line.startsWith(":")) {
      line match {
        case ":q" => false
        case ":p" => {
          println(reserve.repr())
          true
        }
      }
    } else {
      parse(line) match {
        case rule: Rule => {
          println(s"RULE: ${rule.freeze}")
          reserve.addRule(rule)
        }
        case v => {
          println(s"Vertex: ${v.freeze}")
          reserve.addVertex(v)
        }
      }
      this.rewriteMulti()
      println(reserve.repr())
      true
    }
  }

  def rewriteSingle() = {
    reserve.rewriteUntilStop()
  }

  def rewriteMulti() = {
    import degrel.engine.system
    val worker = system.actorOf(RewriteScheduler.props(reserve))
    val future = worker ? RewriteScheduler.Run
    Await.result(future, rewriteTimeout.duration)
  }

  def start() = {
    while (reader.readLine(prompt) match {
      case null => false
      case line => {
        try this.nextLine(line)
        catch {
          case fe: FrontException => {println(s"ERROR: ${fe.msg}"); true}
        }
      }
    }) {}
  }
}
