package degrel.core.transformer

import degrel.core.Vertex

class TryOwnVisitor(owner: Vertex) extends VisitModule {
  override def visit(v: Vertex, visitor: GraphVisitor): Unit = {
    v.tryOwn(owner)
  }
}
