package degrel.core

case class VertexBody(_label: Label, all_edges: Iterable[Edge]) {
  def label: Label = _label

  private val _edge_cache: Map[Label, Iterable[Edge]] = all_edges.groupBy(e => e.label)

  def edges(label: Label): Iterable[Edge] = {
    label match {
      case Label.wildcard => all_edges
      case _ =>
        _edge_cache.get(label) match {
          case None => Seq()
          case Some(a) => a
        }
    }
    if (label == Label.wildcard)
      all_edges
    else
      _edge_cache.get(label) match {
        case None => Seq()
        case Some(a) => a
      }
  }

  override def toString: String = {
    val edgesExpr = all_edges.map(_.toString).mkString(", ")
    s"${label.expr}($edgesExpr)"
  }
}
