package degrel.core.transformer

import degrel.core.Vertex

trait VisitModule {
  def isAcceptable(v: Vertex, visitor: GraphVisitor): Boolean = true

  def visit(v: Vertex, visitor: GraphVisitor)
}
