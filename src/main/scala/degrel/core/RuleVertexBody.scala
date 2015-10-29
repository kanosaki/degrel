package degrel.core

class RuleVertexBody(override val lhs: Vertex, override val rhs: Vertex, override val preds: Seq[Vertex], pragmaEdges: Seq[Edge])
  extends LocalVertexBody(SpecialLabels.V_RULE, Map(), Seq())
  with Rule {

  override val edges: Iterable[Edge] = Seq(
    Edge(this, Label.E.lhs, lhs),
    Edge(this, Label.E.rhs, rhs)) ++
    preds.map(Edge(this, Label.E.pred, _)) ++
    pragmaEdges

  override def asRule: Rule = this
}

object RuleVertexBody {
  def apply(edges: Iterable[Edge]): RuleVertexBody = {
    val (lhs, rhs, preds, pragmaEdges) = Rule.splitEdges(edges)
    new RuleVertexBody(lhs, rhs, preds, pragmaEdges)
  }
}
