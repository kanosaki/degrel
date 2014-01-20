package degrel.core

case class RuleVertexBody(_lhs: Vertex, _rhs: Vertex)
  extends VertexBody("->", Map(), Seq())
          with Rule {

  override def allEdges: Iterable[Edge] = Seq(Edge(this, "_lhs", _lhs),
                                               Edge(this, "_rhs", _rhs))

  def rhs = _rhs

  def lhs = _lhs

  override def reprRecursive(history: Trajectory) = super[Rule].reprRecursive(history)

  override def freeze = {
    RuleVertexBody(lhs.freeze, rhs.freeze)
  }
}
