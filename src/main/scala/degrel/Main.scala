package degrel

import java.io.File

import degrel.control.{CLIArguments, Interpreter}
import degrel.control.console.ConsoleHandle
import degrel.engine.Chassis
import sun.security.pkcs11.Secmod.TrustType

import scala.io.Source


object Main {
  def main(args: Array[String]): Unit = {
    val optParser = CLIArguments.parser()
    optParser.parse(args, CLIArguments()) match {
      case Some(cliArgs) => {
        cliArgs.script match {
          case Some(scriptFile) => {
            val interpreter = new Interpreter(scriptFile)
            interpreter.start()
          }
          case None => {
            val console = new ConsoleHandle(Chassis.createWithMain())
            console.start()
          }
        }
      }
      case None =>
    }
  }
}
