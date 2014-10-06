package degrel.core

class CellBody(vertices: Iterable[Vertex])
  extends VertexBody(
    Label(SpecialLabels.V_CELL),
    Map(), // attrs
    Seq(),
    ID.NA) {
  private val _edges = createEdges(vertices).toSeq

  def createEdges(vertices: Iterable[Vertex]) = {
    vertices.map {
      case r: Rule => Edge(this, SpecialLabels.E_CELL_RULE, r)
      case v => Edge(this, SpecialLabels.E_CELL_ITEM, v)
    }
  }

  override def toString: String = {
    s"{${this.allEdges.map(_.dst).mkString("; ")}}"
  }

  override def allEdges = _edges
}

object CellBody {

}
