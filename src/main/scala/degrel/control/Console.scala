package degrel.control

import degrel.front.{FrontException, ParserUtils}
import degrel.core.{Rule, Vertex}
import degrel.rewriting.LocalReserve
import scala.tools.jline.console.ConsoleReader
import degrel.engine.RewriteScheduler
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{ExecutionContext, Await}
import scala.tools.jline.console.history.FileHistory
import java.util.concurrent.Executors

class Console(val reserve: LocalReserve) {
  private val termParser = ParserUtils

  // Init console reader
  private val reader: ConsoleReader = new ConsoleReader()
  private val history = new FileHistory(env.os.appdir.history)
  reader.setHistory(this.history)

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
    val worker = RewriteScheduler(reserve)
    val future = worker ? RewriteScheduler.Run
    Await.result(future, rewriteTimeout.duration)
  }

  def rewriteBenchmark(maxParallelalism: Int) = {
    for(threadNum <- 1 to maxParallelalism) {
      val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadNum))
      val worker = RewriteScheduler(reserve, ec)
      val future = worker ? RewriteScheduler.Run
      Await.result(future, rewriteTimeout.duration)
    }
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
    history.flush()
  }
}
