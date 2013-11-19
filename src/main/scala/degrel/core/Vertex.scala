package degrel.core

trait Vertex {

}

object Vertex {
}

case class VertexBody(label: Label, all_edges: Seq[Edge]) {
  private val _edge_cache : Map[Label, Seq[Edge]] = all_edges.groupBy(e => e.label)

  def edges(label: Label = null): Seq[Edge] = {
    if(label == null)
      all_edges
    else
      _edge_cache.get(label) match {
        case None => Seq()
        case Some(a) => a
      }
  }
}
