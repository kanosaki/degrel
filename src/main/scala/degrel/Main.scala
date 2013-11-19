package degrel

import java.io.File

object Main {

  def main(args: Array[String]) = {
    print("HELLO WORLD")
  }

  def interpret(path: String): Int = {
    val interpreter = new degrel.Interpreter()
    interpreter.start(new File(path), Array())
  }
}
