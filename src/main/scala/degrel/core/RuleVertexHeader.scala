package degrel.core

class RuleVertexHeader(_lhs: Vertex, _rhs: Vertex)
  extends VertexHeader(RuleVertexBody(_lhs, _rhs)) with Rule {

  private def rBody = this.body.asInstanceOf[RuleVertexBody]

  def rhs = rBody.rhs

  def lhs = rBody.lhs

  override def reprRecursive(history: Trajectory) = super[Rule].reprRecursive(history)
}

