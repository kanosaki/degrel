package degrel.engine.rewriting.molding

import degrel.core.{ValueVertex, VertexHeader, Vertex}

class ValueMolder(prev: Vertex, val context: MoldingContext) extends Molder {
  override val header: VertexHeader = prev.asHeader

  override def mold: Vertex = prev
}
