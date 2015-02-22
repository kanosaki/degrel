package degrel.core

class LocalVertexBody(_label: Label, val attributes: Map[Label, String], _allEdges: Iterable[Edge], _previd: ID) extends VertexBody {
  private[this] val _id = _previd.autoValidate

  override def edges: Iterable[Edge] = _allEdges

  def id: ID = _id

  def label: Label = _label

  override def shallowCopy(): Vertex = ???
}

