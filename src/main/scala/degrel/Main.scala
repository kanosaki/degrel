package degrel

import degrel.control.BootArguments$


object Main {
  def main(args: Array[String]): Unit = {
    val optParser = BootArguments.parser()
    optParser.parse(args, BootArguments()) match {
      case Some(cliArgs) => {
        cliArgs.cmd.start(cliArgs)
      }
      case None =>
    }
  }
}
