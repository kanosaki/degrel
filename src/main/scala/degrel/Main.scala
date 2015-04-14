package degrel

import degrel.control.Interpreter
import degrel.control.console.ConsoleHandle
import degrel.engine.Chassis

import scala.io.Source


object Main {
  def main(args: Array[String]) = {
    val console = new ConsoleHandle(Chassis.createWithMain())
    console.start()
  }
}
