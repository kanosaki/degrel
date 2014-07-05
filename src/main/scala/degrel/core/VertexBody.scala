package degrel.core


import degrel.rewriting.BuildingContext

object VertexBody {
  def apply(label: Label, attributes: Map[String, String], allEdges: Iterable[Edge], id: ID) = {
    label match {
      case Label.reference => new ReferenceVertexBody(label, attributes, allEdges, id)
      case _ => new VertexBody(label, attributes, allEdges, id)
    }
  }
}

class VertexBody(val _label: Label, val attributes: Map[String, String], _allEdges: Iterable[Edge], _previd: ID) extends Vertex {

  private val _id = _previd.autoValidate

  def id: ID = _id

  def label: Label = _label

  private var _edges: Iterable[Edge] = null

  this.writeEdges(_allEdges)

  protected def allEdges: Iterable[Edge] = {
    if (_edges == null) throw new Exception("You cannot refer edges untill initialized.")
    _edges
  }

  def writeEdges(es: Iterable[Edge]) = {
    if(_edges != null) throw new Exception("Duplicated initialization")
    for (e <- es) {
      e.src = this
    }
    _edges = es
  }


  def edges(label: Label): Iterable[Edge] = {
    label match {
      case Label.wildcard => allEdges
      case _ => allEdges.filter(_.label == label)
    }
  }

  def attr(key: String): Option[String] = {
    attributes.get(key)
  }


  def groupedEdges: Iterable[Iterable[Edge]] = {
    allEdges.groupBy(_.label).values
  }

  def repr: String = {
    val id = this.id.shorten
    s"${this.reprLabel}@${id}${this.reprAttrs}"
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
    VertexBody(label, attributes, this.edges(), this.id)
  }
}

