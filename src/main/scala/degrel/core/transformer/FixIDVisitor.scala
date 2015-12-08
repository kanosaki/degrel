package degrel.core.transformer

import degrel.cluster.LocalNode
import degrel.core.{DriverIDSpace, Vertex, VertexHeader}

class FixIDVisitor(idSpace: DriverIDSpace) extends VisitModule {
  override def visit(v: Vertex, visitor: GraphVisitor): Unit = {
    v.asHeader.updateID(idSpace.next())
  }
}
