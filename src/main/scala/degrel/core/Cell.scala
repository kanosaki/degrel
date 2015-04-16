package degrel.core

trait Cell extends Vertex {
  def rules: Seq[Rule]
  def roots: Seq[Vertex]

  def removeRoot(v: Vertex)
  def addRoot(v: Vertex)
}

object Cell {
  def apply(): Cell = {
    Cell(Seq())
  }
  def apply(edges: Iterable[Edge]): Cell = {
    new CellHeader(CellBody(edges))
  }
}

