package degrel

import degrel.control.CLIArguments


object Main {
  def main(args: Array[String]): Unit = {
    val optParser = CLIArguments.parser()
    optParser.parse(args, CLIArguments()) match {
      case Some(cliArgs) => {
        cliArgs.cmd.start(cliArgs)
      }
      case None =>
    }
  }
}
