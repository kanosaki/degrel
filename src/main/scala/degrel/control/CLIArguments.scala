package degrel.control

import java.io.File

case class CLIArguments(script: Option[File] = None,
                        verbose: Boolean = false,
                        cmd: CLICommand = CLICommand.Plain)

/**
 * 引数解析
 */
object CLIArguments {
  def parser() = {
    new scopt.OptionParser[CLIArguments]("degrel") {
      opt[Unit]("verbose").abbr("v").optional().action { (_, c) =>
        c.copy(verbose = true)
      }
      arg[File]("<file>").optional().action { (x, c) =>
        c.copy(script = Some(x))
      }.text("Input script")
      cmd("parse").text("AST Parse").action((_, c) => {
        c.copy(cmd = CLICommand.Parse)
      })
      cmd("bench").text("Benchmark subcommand").children({
        opt[String]("report").abbr("o").optional().action { (path, c) =>
          c.cmd match {
            case cb: CLICommand.Benchmark => c.copy(cmd = cb.copy(outputJson = Some(path)))
            case _ => c.copy(cmd = CLICommand.Benchmark(outputJson = Some(path)))
          }
        }
        arg[String]("<targets ..>").unbounded().optional().action { (x, c) =>
          c.cmd match {
            case cb: CLICommand.Benchmark => c.copy(cmd = cb.copy(targets = cb.targets :+ x))
            case _ => c.copy(cmd = CLICommand.Benchmark(Seq(x)))
          }
        }
      })
    }
  }
}
