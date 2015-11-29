package degrel.core.transformer

import degrel.cluster.LocalNode
import degrel.core.{Vertex, VertexHeader}

class FixIDVisitor(owner: VertexHeader, node: LocalNode) extends VisitModule {
  override def visit(v: Vertex, visitor: GraphVisitor): Unit = {
    v.tryOwn(owner)
  }
}
