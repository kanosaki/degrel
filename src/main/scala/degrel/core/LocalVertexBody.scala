package degrel.core

class LocalVertexBody(override val label: Label, val attributes: Map[Label, String], override val edges: Iterable[Edge]) extends VertexBody {
  this.edges.foreach { e =>
    e.dst match {
      case vh: VertexHeader => vh.addRevrseNeighbor(this)
    }
  }

  override def shallowCopy(): Vertex = ???
}

