package degrel.core

case class RuleVertexBody(_lhs: Vertex, _rhs: Vertex, _id: ID)
  extends VertexBody(SpecialLabels.V_RULE, Map(), Seq(), _id)
  with Rule {

  override def allEdges: Iterable[Edge] = Seq(
    Edge(this, SpecialLabels.E_LHS, _lhs),
    Edge(this, SpecialLabels.E_RHS, _rhs))

  override def reprRecursive(history: Trajectory) = super[Rule].reprRecursive(history)

  def rhs = _rhs

  def lhs = _lhs
}
