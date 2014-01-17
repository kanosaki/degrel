package degrel.core


import degrel.rewriting.BuildingContext

object VertexBody {
  def apply(label: Label, attributes: Map[String, String], all_edges: Iterable[Edge]) = {
    label match {
      case Label.reference => new ReferenceVertexBody(label, attributes, all_edges)
      case _ => new VertexBody(label, attributes, all_edges)
    }
  }
}

class VertexBody(val _label: Label, val attributes: Map[String, String], val all_edges: Iterable[Edge]) extends Vertex {
  def label: Label = _label

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
        if (all_edges.isEmpty) {
          s"${this.repr}"
        } else {
          val edgesExpr = all_edges.map(_.reprRecursive(nextHistory)).mkString(", ")
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
  def build(context: BuildingContext): Vertex = {
    val buildEdges = this.edges().map(_.build(context))
    Vertex(this.label.expr, buildEdges, this.attributes)
  }

  def freezeRecursive(footprints: Footprints[Vertex]): Vertex = {
    footprints.stamp(this) {
      case Right(fp) => {
        val frozenEdges = all_edges.map(_.freezeRecursive(fp))
        VertexBody(label, attributes, frozenEdges)
      }
      case Left(fp) => fp.resultOf(this)
    }
  }

  def copyRecursive(footprints: Footprints[Vertex]): Vertex = {
    footprints.stamp(this) {
      case Right(fp) => {
        val frozenEdges = all_edges.map(_.copyRecursive(fp))
        VertexBody(label, attributes, frozenEdges)
      }
      case Left(fp) => fp.resultOf(this)
    }
  }

  def shallowCopy: Vertex = {
    VertexBody(label, attributes, all_edges)
  }
}

