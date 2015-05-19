package degrel

import java.io.File

import degrel.control.Interpreter
import degrel.control.console.ConsoleHandle
import degrel.engine.Chassis

import scala.io.Source


object Main {
  def main(args: Array[String]): Unit = {
    if (args.length == 1) {
      val interpreter = new Interpreter(new File(args(0)))
      interpreter.start()
    } else {
      val console = new ConsoleHandle(Chassis.createWithMain())
      console.start()
    }
  }
}
