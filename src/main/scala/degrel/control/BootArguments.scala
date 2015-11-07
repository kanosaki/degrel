package degrel.control

import java.io.File

case class BootArguments(script: Option[File] = None,
                         verbose: Boolean = false,
                         cluster: Boolean = false,
                         name: Option[String] = None,
                         config: Option[String] = None,
                         seeds: Seq[String] = Seq(),
                         cmd: CLICommand = CLICommand.Plain,
                         options: Map[String, String] = Map()) {
  def rewriteeSetName: String = {
    options.getOrElse("rewriteeset", "plain")
  }

  def createBootstrapper(): Bootstrapper = {
    Bootstrapper(this)
  }
}

/**
  * 引数解析
  */
object BootArguments {
  /**
    * --optionsで渡されるKey-Value pairをSystem.propertyへも上書きするかどうかを指定します
    */
  val MIRROR_NAME_OPTS_TO_SYSPROP = true

  def parser() = {
    new scopt.OptionParser[BootArguments]("degrel") {
      // -------------------------------------------------------------------------------
      // Common options
      // -------------------------------------------------------------------------------
      opt[Unit]("verbose").abbr("v").optional().action { (_, c) =>
        c.copy(verbose = true)
      }
      opt[Unit]("cluster").optional().action { (_, c) =>
        c.copy(cluster = true)
      }
      opt[String]("name").abbr("n").optional().action { (n, c) =>
        c.copy(name = Some(n))
      }
      opt[String]("config").abbr("n").optional().action { (n, c) =>
        c.copy(name = Some(n))
      }
      opt[Seq[String]]("seeds").abbr("s").valueName("host1,host2,...").action((x, c) => {
        c.copy(seeds = x)
      }).text("akka seed nodes")
      opt[Map[String, String]]("options").abbr("D").valueName("k1=v1,k2=v2,...").action((x, c) => {
        if (MIRROR_NAME_OPTS_TO_SYSPROP) {
          x.foreach(kv => {
            System.setProperty(kv._1, kv._2)
          })
        }
        c.copy(options = x)
      }).text("versatile parameters")
      arg[File]("<file>").optional().action { (x, c) =>
        c.copy(script = Some(x))
      }.text("Input script")
      // -------------------------------------------------------------------------------
      // Parse subcommand
      // -------------------------------------------------------------------------------
      cmd("parse").text("AST Parse").action((_, c) => {
        c.copy(cmd = CLICommand.Parse)
      })
      // -------------------------------------------------------------------------------
      // Benchmark subcommand
      // -------------------------------------------------------------------------------
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
      // -------------------------------------------------------------------------------
      // Cluster node subcommand
      // -------------------------------------------------------------------------------
      cmd("cluster").text("degrel Cluster").action { (_, c) =>
        c.copy(cmd = CLICommand.Cluster())
      }
    }
  }
}
