package degrel.core

class RuleVertexHeader(_lhs: Vertex, _rhs: Vertex, _id: ID)
  extends LocalVertexHeader(RuleVertexBody(_lhs, _rhs, _id)) with Rule {

  def rhs = rBody.rhs

  private def rBody = this.body.asInstanceOf[RuleVertexBody]

  def lhs = rBody.lhs

  override def reprRecursive(history: Trajectory) = super[Rule].reprRecursive(history)
}

