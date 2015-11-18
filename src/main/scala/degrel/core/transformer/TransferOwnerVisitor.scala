package degrel.core.transformer

import degrel.core.Vertex

class TransferOwnerVisitor(owner: Vertex) extends VisitModule {
  override def visit(v: Vertex, visitor: GraphVisitor): Unit = {
    v.transferOwner(owner)
  }
}
