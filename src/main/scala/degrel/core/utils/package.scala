package degrel.core

package object utils {
  def pp(v: Vertex, multiLine: Boolean = false): String = {
    val printer = new PrettyPrinter(v)
    multiLine match {
      case true => printer.multiLine
      case false => printer.singleLine
    }
  }

  def prettyPrint = pp _
}
