package degrel.core

class CellBody(edges: Iterable[Edge])
  extends VertexBody(
    Label(SpecialLabels.V_CELL),
    Map(), // attrs
    edges,
    ID.NA) {

  override def toString: String = {
    s"{${this.allEdges.map(_.dst).mkString("; ")}}"
  }
}

object CellBody {

}
