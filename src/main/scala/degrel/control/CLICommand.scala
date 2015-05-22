package degrel.control

import java.nio.file.Paths

import degrel.control.console.ConsoleHandle
import degrel.engine.Chassis
import degrel.misc.benchmark.FilesBenchmark


sealed trait CLICommand {
  def start(arg: CLIArguments): Unit
}

object CLICommand {

  case class Benchmark(targets: Seq[String] = Seq(), outputJson: Option[String] = None) extends CLICommand {
    override def start(arg: CLIArguments): Unit = {
      val bench = new FilesBenchmark(targets.map(Paths.get(_)), outputJson.map(Paths.get(_)))
      bench.start()
    }
  }

  case object Plain extends CLICommand {
    override def start(arg: CLIArguments): Unit = {
      arg.script match {
        case Some(scriptFile) => {
          val interpreter = new Interpreter(
            mainFile = scriptFile)
          interpreter.start()
        }
        case None => {
          val console = new ConsoleHandle(Chassis.createWithMain())
          console.start()
        }
      }
    }
  }

}

