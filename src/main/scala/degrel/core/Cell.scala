package degrel.core

trait Cell extends Vertex {
  def rules: Seq[Rule]
  def roots: Seq[Vertex]

  def removeRoot(v: Vertex)
  def addRoot(v: Vertex)
}

object Cell {
  def apply(edges: Iterable[Edge]) = {
    new CellHeader(CellBody(edges))
  }
}

