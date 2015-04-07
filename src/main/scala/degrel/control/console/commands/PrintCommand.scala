package degrel.control.console.commands

import degrel.control.console.ConsoleHandle
import degrel.core.utils.PrettyPrintOptions

class PrintCommand extends ConsoleCommand {
  override def name: String = "print"

  override def shortName: Option[String] = Some("p")

  implicit val ppOpt = PrettyPrintOptions(multiLine = true)

  override def execute(argLine: String, console: ConsoleHandle): Unit = {
    console.println(console.current.cell.pp)
  }
}
