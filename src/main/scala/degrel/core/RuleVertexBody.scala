package degrel.core

case class RuleVertexBody(_lhs: Vertex, _rhs: Vertex)
  extends VertexBody("->",
                      Map(),
                      Stream(Edge("_lhs", _lhs),
                              Edge("_rhs", _rhs)))
          with Rule {
  def rhs = _rhs

  def lhs = _lhs

  override def reprRecursive(history: Trajectory) = super[Rule].reprRecursive(history)

  override def freeze = {
    RuleVertexBody(lhs.freeze, rhs.freeze)
  }
}
