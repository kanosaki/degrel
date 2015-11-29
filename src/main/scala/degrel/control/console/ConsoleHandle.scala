package degrel.control.console

import degrel.control.HandleBase
import degrel.control.console.commands.ConsoleCommand
import degrel.engine.Chassis
import degrel.utils.PrettyPrintOptions
import jline.console.ConsoleReader

import scala.concurrent.Await
import scala.concurrent.duration._

class ConsoleHandle(val chassis: Chassis, commandSet: Option[Seq[ConsoleCommand]] = None) extends HandleBase {
  private var isQuitting = false
  val coms = new CommandsManager(commandSet.getOrElse(commands.default))
  val console = new ConsoleReader()
  val config = new ConsoleConfig()
  implicit val ppOpts = PrettyPrintOptions(multiLine = true)

  this.setCurrent(chassis.main)

  def start() = {
    while (!isQuitting) {
      val line = console.readLine(s"$currentName> ")
      if (line == null) {
        isQuitting = true
      } else {
        this.nextLine(line.trim)
      }
    }
  }

  private def nextLine(line: String) = {
    if (line.startsWith(commands.COMMAND_PREFIX)) {
      this.dispatchCommand(line)
    } else {
      this.evalLine(line)
      this.afterEval()
    }
  }

  private def dispatchCommand(line: String) = {
    coms.handle(line.substring(commands.COMMAND_PREFIX.length), this)
  }

  protected def evalLine(line: String) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    try {
      val input = degrel.parseVertex(line)
      Await.result(this.current.send(input), 10.seconds)
    } catch {
      case e: Throwable => console.println(e.toString)
    }
  }

  private def afterEval() = {
    if (config.rewriteOnReturn) {
      this.current.stepUntilStop(100)
    }
    if (config.printOnReturn) {
      this.println(this.current.cell.pp)
    }
  }

  def quit() = {
    isQuitting = true
  }

  def println(line: String) = {
    console.println(line)
  }

  def print(txt: String) = {
    console.print(txt)
  }
}
