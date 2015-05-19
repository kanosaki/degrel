package degrel.control

import java.io.File

case class CLIArguments(script: Option[File] = None)

object CLIArguments {
  def parser() = {
    new scopt.OptionParser[CLIArguments]("degrel") {
      arg[File]("<file>").optional().action { (x, c) =>
        c.copy(script = Some(x))
      }.text("Input script")
    }
  }
}
