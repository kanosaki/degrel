package degrel

import degrel.rewriting.LocalReserve
import degrel.control.Interpreter
import scala.io.Source


object Main {
  def main(args: Array[String]) = {
    if(args.length == 1) {
      this.startInterpreter(args(0))
    } else {
      this.startConsole()
    }
    degrel.engine.system.shutdown()
  }

  def startInterpreter(file: String) = {
    val src = Source.fromFile(file)
    val interpreter = new Interpreter(src)
    interpreter.start()
    src.close()
  }

  def startConsole() = {
    val reserve = new LocalReserve()
    val console = new degrel.control.Console(reserve)
    console.start()
  }
}
