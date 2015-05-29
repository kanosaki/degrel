package degrel.core

import degrel.utils.PrettyPrintOptions

package object utils {
  def pp(v: Vertex)(implicit opts: PrettyPrintOptions = PrettyPrintOptions.default): String = {
    val printer = new PrettyPrinter(v)
    printer()
  }

  def prettyPrint = pp _
}
