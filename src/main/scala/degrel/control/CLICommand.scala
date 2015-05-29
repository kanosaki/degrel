package degrel.control

import java.nio.file.Paths

import degrel.control.console.ConsoleHandle
import degrel.engine.Chassis
import degrel.misc.benchmark.FilesBenchmark
import jline.console.ConsoleReader


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

  case object Parse extends CLICommand {
    override def start(arg: CLIArguments): Unit = {
      val console = new ConsoleReader()
      while (true) {
        val line = console.readLine(s"parse> ")
        if (line != null) {
          try {
            val ast = degrel.front.Parser.vertex(line)
            println(ast)
          } catch {
            case ex: Throwable => {
              System.err.println(s"Message: ${ex.getMessage}")
              ex.printStackTrace()
            }
          }
        } else {
          return
        }
      }
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

