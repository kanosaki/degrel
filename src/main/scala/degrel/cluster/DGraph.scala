package degrel.cluster

import degrel.core.{ID, Label}

case class DGraph(root: ID, vertices: Seq[DVertex]) {

}

case class DEdge(label: String, dst: ID)

trait DVertex {
  def id: ID

  def label: String

  def attributes: Seq[(String, String)]

  def edges: Seq[DEdge]
}

case class DPlainVertex(id: ID,
                        label: String,
                        attributes: Seq[(String, String)],
                        edges: Seq[DEdge]) extends DVertex

case class DRule(id: ID,
                 lhs: ID,
                 rhs: ID,
                 pragmaEdges: Seq[DEdge],
                 attributes: Seq[(String, String)]) extends DVertex {

  override def label: String = Label.V.rule.expr

  override def edges: Seq[DEdge] = Seq(
    DEdge(Label.E.lhs.expr, this.lhs),
    DEdge(Label.E.rhs.expr, this.rhs))
}

case class DCell(id: ID,
                 attributes: Seq[(String, String)],
                 edges: Seq[DEdge],
                 binding: Seq[(ID, ID)]) extends DVertex {
  override def label: String = Label.V.cell.expr
}


