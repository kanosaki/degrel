package degrel.control.console

package object commands {
  val COMMAND_PREFIX = ":"

  def default: Seq[ConsoleCommand] = Seq(
    new PrintCommand(),
    new RewriteCommand(),
    new ReprCommand()
    )
}
