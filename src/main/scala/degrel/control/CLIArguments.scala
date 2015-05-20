package degrel.control

import java.io.File

case class CLIArguments(script: Option[File] = None, reportStatistics: Boolean = false)

object CLIArguments {
  def parser() = {
    new scopt.OptionParser[CLIArguments]("degrel") {
      opt[Unit]("report").optional().action { (_, c) =>
        c.copy(reportStatistics = true)
      }
      arg[File]("<file>").optional().action { (x, c) =>
        c.copy(script = Some(x))
      }.text("Input script")
    }
  }
}
