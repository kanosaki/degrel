
/**
 * 命名規則:
 */
package object degrel {

  def parseVertex(s: String): core.Vertex = {
    val ast = front.Parser.vertex(s)
    graphbuilder.build[core.Vertex](ast)
  }
}
