package degrel.core


import degrel.engine._

case class VertexBody(_label: Label, attributes: Map[String, String], all_edges: Iterable[Edge]) extends Vertex {
  def label: Label = _label

  override def equals(other: Any) = other match {
    case vh: VertexHeader => vh.body == this
    case vb: VertexBody => this.checkEquals(vb)
    case _ => false
  }

  def checkEquals(other: VertexBody): Boolean = {
    if (this.label != other.label) return false
    if (this.attributes != other.attributes) return false
    val thisEdges = this.edges().toSet
    val otherEdges = other.edges().toSet
    thisEdges == otherEdges
  }

  override def hashCode = {
    val prime = 41
    var result = 1
    result = prime * result + label.hashCode()
    result = prime * result + attributes.hashCode()
    result = prime * result + all_edges.hashCode()
    result
  }

  private val _edge_cache: Map[Label, Iterable[Edge]] = all_edges.groupBy(e => e.label)
  protected val hasPolyvalentEdge = all_edges.size != _edge_cache.size

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

  def attr(key: String): Option[String] = {
    attributes.get(key)
  }


  def groupedEdges: Iterable[Iterable[Edge]] = {
    _edge_cache.values
  }

  override def toString = this.repr


  def repr: String = {
    val attrsExpr = if (attributes.isEmpty) "" else this.reprAttrs
    s"${label.expr}$attrsExpr"
  }

  def reprRecursive: String = {
    val edgesExpr = all_edges.map(_.toString).mkString(", ")
    s"${this.repr}($edgesExpr)"
  }

  def reprAttrs: String = {
    val kvsExpr = attributes.map {case (k, v) => s"$k:$v"}.mkString(", ")
    s"{$kvsExpr}"
  }

  // Perform as RhsVertex
  def build(context: BuildingContext): Vertex = {
    if (this.isReference) {
      context.matchOf(this.referenceTarget)
    } else {
      val buildEdges = this.edges().map(_.build(context))
      Vertex(this.label.expr, buildEdges, this.attributes)
    }
  }
}
