package degrel.control

import java.util.concurrent.Executors

import akka.pattern.ask
import akka.util.Timeout
import degrel.core.{Rule, Vertex}
import degrel.engine.RewriteScheduler
import degrel.front.{FrontException, ParserUtils}
import degrel.engine.rewriting.LocalReserve
import jline.console.ConsoleReader
import jline.console.history.FileHistory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class Console(val reserve: LocalReserve) {
  val prompt = ">>> "
  implicit val rewriteTimeout = Timeout(10.seconds)
  private[this] val termParser = ParserUtils
  // Init console reader
  private[this] val reader: ConsoleReader = new ConsoleReader()
  private[this] val history = new FileHistory(env.os.appdir.history)

  reader.setHistory(this.history)

  def rewriteSingle() = {
    reserve.rewriteUntilStop()
  }

  def rewriteBenchmark(maxParallelalism: Int) = {
    for (threadNum <- 1 to maxParallelalism) {
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
          case fe: FrontException => {
            println(s"ERROR: ${fe.msg}");
            true
          }
        }
      }
    }) {}
    history.flush()
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
          println(s"RULE: $rule")
          reserve.addRule(rule)
        }
        case v => {
          println(s"Vertex: $v")
          reserve.addVertex(v)
        }
      }
      this.rewriteMulti()
      println(reserve.repr())
      true
    }
  }

  def parse(s: String): Vertex = {
    degrel.parseVertex(s)
  }

  def rewriteMulti() = {
    val worker = RewriteScheduler(reserve)
    val future = worker ? RewriteScheduler.Run
    Await.result(future, rewriteTimeout.duration)
  }
}
