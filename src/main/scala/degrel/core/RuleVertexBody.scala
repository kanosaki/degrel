package degrel.core

class RuleVertexBody(override val lhs: Vertex, override val rhs: Vertex, pragmaEdges: Seq[Edge])
  extends LocalVertexBody(SpecialLabels.V_RULE, Map(), Seq())
  with Rule {

  override val edges: Iterable[Edge] = Seq(
    Edge(this, SpecialLabels.E_LHS, lhs),
    Edge(this, SpecialLabels.E_RHS, rhs)) ++ pragmaEdges

  override def asRule: Rule = this
}

object RuleVertexBody {
  def apply(edges: Iterable[Edge]): RuleVertexBody = {
    val (lhs, rhs, pragmaEdges) = Rule.splitEdges(edges)
    new RuleVertexBody(lhs, rhs, pragmaEdges)
  }
}
