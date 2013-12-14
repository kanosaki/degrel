package degrel

import degrel.rewriting.LocalReserve


object Main {

  def main(args: Array[String]) = {
    val reserve = new LocalReserve()
    val console = new degrel.control.Console(reserve)
    console.start()
  }
}
