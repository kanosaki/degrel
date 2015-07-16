package degrel.core

trait Cell extends Vertex {
  def rules: Seq[Rule]
  def roots: Seq[Vertex]

  def otherEdges: Seq[Edge]

  /**
   * このCellを直接内包するCell
   */
  def parent: Cell

  /**
   * この`Cell`の元になるCell．
   * 規則を継承します
   */
  def bases: Seq[Cell]

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

