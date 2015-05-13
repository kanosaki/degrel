package degrel.engine.resource

import java.io.{InputStream, OutputStream, PrintStream}

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
