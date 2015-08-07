package degrel.dgspec

import java.io.{ByteArrayOutputStream, PrintStream}

import degrel.control.Interpreter
import degrel.engine.Chassis
import degrel.engine.sphere.DefaultConsole

class SpecInterpreter(ch: Chassis) extends Interpreter(ch) {
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
