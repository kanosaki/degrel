package degrel.core

case class RuleVertexBody(_lhs: Vertex, _rhs: Vertex)
  extends LocalVertexBody(SpecialLabels.V_RULE, Map(), Seq())
  with Rule {

  override val edges: Iterable[Edge] = Seq(
    Edge(this, SpecialLabels.E_LHS, _lhs),
    Edge(this, SpecialLabels.E_RHS, _rhs))

  override def asRule: Rule = this

  def rhs = _rhs

  def lhs = _lhs
}
