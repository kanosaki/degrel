package degrel.control.console.commands

import degrel.control.console.ConsoleHandle

trait ConsoleCommand {
  def name: String
  def shortName: Option[String]
  def execute(argLine: String, console: ConsoleHandle): Unit
}
