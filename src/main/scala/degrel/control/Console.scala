package degrel.control

import degrel.front.{FrontException, ParserUtils}
import degrel.core.{Rule, Vertex}
import degrel.rewriting.LocalReserve
import scala.tools.jline.console.ConsoleReader

class Console(val reserve: LocalReserve) {
  private val termParser = ParserUtils
  private val reader: ConsoleReader = new ConsoleReader()
  val prompt = ">>> "

  def parse(s: String): Vertex = {
    termParser.parseVertex(s)
  }

  def nextLine(line: String) = {
    if (line.startsWith(":")) {
      line != ":q"
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
      reserve.rewriteUntilStop()
      println(reserve.repr())
      true
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
  }
}
