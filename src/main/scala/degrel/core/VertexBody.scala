package degrel.core


import degrel.engine.rewriting.BuildingContext

object VertexBody {
  def apply(label: Label, attributes: Map[Label, String], allEdges: Iterable[Edge], id: ID): VertexBody = {
    label match {
      case Label.V.reference => new ReferenceVertexBody(label, attributes, allEdges)
      case _ => new LocalVertexBody(label, attributes, allEdges)
    }
  }
}

trait VertexBody extends Vertex {
  def attr(key: Label): Option[String] = {
    attributes.get(key)
  }

  def reprLabel: String = {
    this.attributes.get("__captured_as__") match {
      case Some(capExpr) => s"$capExpr[${this.label.expr}]"
      case None => s"${this.label.expr}"
    }
  }

  def reprAttrs: String = {
    val targetKvs = attributes
      .filter { case (k, v) => !k.isMeta}
    if (targetKvs.nonEmpty) {
      val kvsExpr = targetKvs.map { case (k, v) => s"$k:$v"}.mkString(", ")
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
    if (this.hasEdge(Label.E.others)) {
      val plainEdges = this.edges.filter(_.label != Label.E.others)
      val matchedV = this.thruSingle(Label.E.others) // TODO: Error handle: 二つ以上OthersEdgeが存在した場合
      val unmatchedEdges = context.unmatchedEdges(matchedV)
      val builtEdges = unmatchedEdges ++ plainEdges.map(_.build(context))
      Vertex(this.label.expr, builtEdges.toSeq, this.attributes)
    } else {
      val buildEdges = this.edges.map(_.build(context))
      Vertex(this.label.expr, buildEdges.toSeq, this.attributes)
    }
  }

  override def id: ID = ???
}

