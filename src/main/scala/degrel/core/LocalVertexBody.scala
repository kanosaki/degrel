package degrel.core

class LocalVertexBody(_label: Label, val attributes: Map[Label, String], _allEdges: Iterable[Edge]) extends VertexBody {
  _allEdges.foreach { e =>
    e.dst match {
      case vh: VertexHeader => vh.addRevrseNeighbor(this)
    }
  }

  override def edges: Iterable[Edge] = _allEdges

  def label: Label = _label

  override def shallowCopy(): Vertex = ???
}

