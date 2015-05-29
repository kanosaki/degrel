package degrel.control.console.commands

import degrel.control.console.ConsoleHandle

class RewriteCommand extends ConsoleCommand {
  override def name: String = "rewrite"

  override def shortName: Option[String] = Some("rw")

  override def execute(argLine: String, console: ConsoleHandle): Unit = {
    console.current.stepUntilStop(100)
  }
}
