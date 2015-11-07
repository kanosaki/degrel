package degrel.control.cluster.commands

import degrel.control.console.ConsoleHandle
import degrel.control.console.commands.ConsoleCommand

class ClusterCommands extends ConsoleCommand {
  override def name: String = "cluster"

  override def shortName: Option[String] = Some("c")

  override def execute(argLine: String, console: ConsoleHandle): Unit = {
  }
}
