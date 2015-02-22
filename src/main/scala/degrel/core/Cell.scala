package degrel.core

trait Cell extends Vertex {
  def rules: Seq[Rule]
  def roots: Seq[Vertex]
}

object Cell {
  def apply(edges: Iterable[Edge]) = {
    val rules = edges.filter(_.label == Label.E.cellRule).map(_.dst.asRule)
    val roots = edges.filter(_.label == Label.E.cellItem).map(_.dst)
    val body = new CellBody(roots, rules)
    new CellHeader(body)
  }
}

