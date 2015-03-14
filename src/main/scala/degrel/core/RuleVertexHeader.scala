package degrel.core

class RuleVertexHeader(_lhs: Vertex, _rhs: Vertex)
  extends LocalVertexHeader(RuleVertexBody(_lhs, _rhs)) with Rule {

  def rhs = rBody.rhs

  private def rBody = this.body.asInstanceOf[RuleVertexBody]

  def lhs = rBody.lhs
}

