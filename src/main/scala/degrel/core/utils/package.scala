package degrel.core

package object utils {
  def pp(v: Vertex)(implicit opts: PrettyPrintOptions = PrettyPrintOptions.default): String = {
    val printer = new PrettyPrinter(v)
    printer()
  }

  def prettyPrint = pp _
}
