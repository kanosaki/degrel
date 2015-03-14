package degrel.core

class LocalVertexBody(_label: Label, val attributes: Map[Label, String], _allEdges: Iterable[Edge]) extends VertexBody {

  override def edges: Iterable[Edge] = _allEdges

  def label: Label = _label

  override def shallowCopy(): Vertex = ???
}

