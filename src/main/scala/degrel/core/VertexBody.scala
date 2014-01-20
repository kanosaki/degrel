package degrel.core


import degrel.rewriting.BuildingContext

object VertexBody {
  def apply(label: Label, attributes: Map[String, String], allEdges: Iterable[Edge]) = {
    label match {
      case Label.reference => new ReferenceVertexBody(label, attributes, allEdges)
      case _ => new VertexBody(label, attributes, allEdges)
    }
  }
}

class VertexBody(val _label: Label, val attributes: Map[String, String], _allEdges: Iterable[Edge]) extends Vertex {

  private lazy val _edge_cache: Map[Label, Iterable[Edge]] = allEdges.groupBy(e => e.label)
  protected lazy val hasPolyvalentEdge = allEdges.size != _edge_cache.size
  for (e <- _allEdges) {
    e.src = this
  }

  def label: Label = _label

  protected def allEdges: Iterable[Edge] = {
    _allEdges
  }

  def isSameElement(other: Element): Boolean = other match {
    case vh: VertexHeader => vh.body ==~ this
    case vb: VertexBody => this.checkIsSame(vb)
    case _ => false
  }

  private def checkIsSame(other: VertexBody): Boolean = {
    if (this.label != other.label) return false
    val thisEdges = this.edges().map(new EdgeEqualityAdapter(_)).toSet
    val otherEdges = other.edges().map(new EdgeEqualityAdapter(_)).toSet
    thisEdges == otherEdges
  }

  override def equals(other: Any) = other match {
    case vh: VertexHeader => vh.body == this
    case vb: VertexBody => this.checkEquals(vb)
    case _ => false
  }

  private def checkEquals(other: VertexBody): Boolean = {
    if (this.label != other.label) return false
    val thisEdges = this.edges().toSet
    val otherEdges = other.edges().toSet
    thisEdges == otherEdges
  }

  override def hashCode = {
    val prime = 41
    var result = 1
    result = prime * result + label.hashCode()
    result = prime * result + allEdges.hashCode()
    result
  }


  def edges(label: Label): Iterable[Edge] = {
    label match {
      case Label.wildcard => allEdges
      case _ =>
        _edge_cache.get(label) match {
          case None => Seq()
          case Some(a) => a
        }
    }
    if (label == Label.wildcard)
      allEdges
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

  def repr: String = {
    s"${this.reprLabel}${this.reprAttrs}"
  }


  def reprLabel: String = {
    this.attributes.get("__captured_as__") match {
      case Some(capExpr) => s"$capExpr[${this.label.expr}]"
      case None => s"${this.label.expr}"
    }
  }

  def reprRecursive(trajectory: Trajectory): String = {
    trajectory.walk(this) {
      case Right(nextHistory) => {
        if (allEdges.isEmpty) {
          s"${this.repr}"
        } else {
          val edgesExpr = this.edges().map(_.reprRecursive(nextHistory)).mkString(", ")
          s"${this.repr}($edgesExpr)"
        }
      }
      case Left(_) => {
        this.repr
      }
    }
  }

  def reprAttrs: String = {
    val targetKvs = attributes
      .filter {case (k, v) => !k.startsWith("_")}
    if (!targetKvs.isEmpty) {
      val kvsExpr = targetKvs.map {case (k, v) => s"$k:$v"}.mkString(", ")
      s"{$kvsExpr}"
    } else {
      ""
    }
  }

  // Perform as RhsVertex
  /**
   * 自身を書き換え右辺の頂点として，新規に頂点を構成します
   * 自分が左辺でマッチしている頂点，すなわち対応するredex上の頂点を持つ場合は，頂点をマージします
   * また，マッチしていない場合は新規に頂点を構築します
   */
  def build(context: BuildingContext): Vertex = {
    context.matchedVertex(this) match {
      case Some(matchedV) => {
        val matchedEdges = this.edges().map(context.matchedEdgeExact).toSet
        val builtEdges = matchedV
                           .edges()
                           .filter(!matchedEdges.contains(_))
                           .map(_.duplicate()) ++
                         this.edges()
                           .map(_.build(context))
        Vertex(matchedV.label.expr, builtEdges, matchedV.attributes)
      }
      case None => {
        val buildEdges = this.edges().map(_.build(context))
        Vertex(this.label.expr, buildEdges, this.attributes)
      }
    }
  }

  def shallowCopy: Vertex = {
    VertexBody(label, attributes, this.edges())
  }
}

