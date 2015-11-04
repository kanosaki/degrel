package degrel.control.console.commands

import degrel.control.console.ConsoleHandle
import degrel.utils.PrettyPrintOptions

class ReprCommand extends ConsoleCommand{
  override def name: String = "repr"

  override def shortName: Option[String] = None

  override def execute(argLine: String, console: ConsoleHandle): Unit = {
    implicit val opts = PrettyPrintOptions(showAllId = true, multiLine = true)
    console.println(console.current.cell.pp)
  }
}
