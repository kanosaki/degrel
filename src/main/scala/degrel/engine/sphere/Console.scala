package degrel.engine.sphere

import java.io.{InputStream, PrintStream}

import org.apache.commons.io.output.NullOutputStream

trait Console {
  def stdout: PrintStream

  def stdin: InputStream

  def stderr: PrintStream
}

class DefaultConsole extends Console {
  override def stdout: PrintStream = System.out

  override def stdin: InputStream = System.in

  override def stderr: PrintStream = System.err
}

class QuietConsole extends DefaultConsole {
  override val stdout: PrintStream = new PrintStream(new NullOutputStream())
}
