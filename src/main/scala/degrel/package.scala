import degrel.core.Cell

/**
 * 命名規則:
 */
package object degrel {

  def parseVertex(s: String): core.Vertex = {
    val ast = front.Parser.vertex(s)
    graphbuilder.build[core.Vertex](ast)
  }

  def parseCell(s: String): Cell = {
    val ast = front.Parser.cell(s)
    graphbuilder.build[core.Cell](ast)
  }
}
