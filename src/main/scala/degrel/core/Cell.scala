package degrel.core

trait Cell extends Vertex {
  def imports: Seq[Cell] = ???
  def rules: Seq[Rule] = this.thru(Label.E.cellRule).map(_.asRule).toSeq
}

object Cell {
  def apply(edges: Iterable[Edge]) = {
    val body = new CellBody(edges)
    new CellHeader(body)
  }
}

