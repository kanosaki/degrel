package degrel.cluster

import degrel.core.{ID, Label}

case class DGraph(root: DID, vertices: Vector[DVertex], idMap: Vector[(DID, ID)]) {

  def pp: String = {
    val idMap = vertices.map(v => v.id -> v).toMap
    val sb = new StringBuilder()
    sb ++= s"Root: $root\n"
    vertices.foreach { v =>
      sb ++= f"${v.id} ${v.label}\n"
      v.edges.foreach { e =>
        val dstLabel = idMap.get(e.dst).map(_.label).getOrElse("--")
        sb ++= f"  | ${e.label} --> ${e.dst}($dstLabel)\n"
      }
    }
    sb.toString()
  }
}

case class DEdge(label: String, dst: DID)

trait DVertex {
  def id: DID

  def label: String

  def attributes: Seq[(String, String)]

  def edges: Seq[DEdge]
}

case class DPlainVertex(id: DID,
                        label: String,
                        attributes: Seq[(String, String)],
                        edges: Seq[DEdge]) extends DVertex

case class DRule(id: DID,
                 lhs: DID,
                 rhs: DID,
                 preds: Seq[DID],
                 pragmaEdges: Seq[DEdge],
                 attributes: Seq[(String, String)]) extends DVertex {

  override def label: String = Label.V.rule.expr

  override def edges: Seq[DEdge] = Seq(
    DEdge(Label.E.lhs.expr, this.lhs),
    DEdge(Label.E.rhs.expr, this.rhs)) ++ pragmaEdges
}

case class DCell(id: DID,
                 attributes: Seq[(String, String)],
                 edges: Seq[DEdge],
                 binding: Seq[(ID, ID)]) extends DVertex {
  override def label: String = Label.V.cell.expr
}


