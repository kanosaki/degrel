package degrel.control.cluster.commands

import degrel.control.cluster.ClusterConsole
import degrel.control.console.ConsoleHandle
import degrel.control.console.commands.ConsoleCommand

class JoinNodes extends ConsoleCommand {
  override def name: String = "join"

  override def shortName: Option[String] = None

  override def execute(argLine: String, console: ConsoleHandle): Unit = {
    val cc = console.asInstanceOf[ClusterConsole]
  }
}

object JoinNodes {
  def apply() = {
    new JoinNodes()
  }
}