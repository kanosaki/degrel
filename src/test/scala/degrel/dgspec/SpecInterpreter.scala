package degrel.dgspec

import java.io.{File, PrintStream, ByteArrayOutputStream}

import degrel.control.Interpreter
import degrel.engine.sphere.DefaultConsole

class SpecInterpreter(file: File) extends Interpreter(file) {
  val console = new BufferingOutputConsole()

  def lastOutput = console.byteArrayOutput.toString

  override def onStarting(): Unit = {
    chassis.sphere.console = this.console
  }
}

class BufferingOutputConsole extends DefaultConsole {
  val byteArrayOutput = new ByteArrayOutputStream()

  override val stdout: PrintStream = new PrintStream(byteArrayOutput)
}
