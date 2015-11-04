package degrel.core.transformer

import degrel.core.{Label, Vertex}

class CellLimiter extends VisitModule {

  override def isAcceptable(v: Vertex, visitor: GraphVisitor): Boolean = {
    v.label != Label.V.cell
  }

  override def visit(v: Vertex, visitor: GraphVisitor): Unit = {

  }
}

object CellLimiter {
  val default = new CellLimiter()

}
