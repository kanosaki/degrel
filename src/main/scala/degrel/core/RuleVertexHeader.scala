package degrel.core

class RuleVertexHeader(bdy: VertexBody, _initID: ID = ID.nextLocalVertexID())
  extends LocalVertexHeader(bdy, _initID) with Rule {

  private def rBody: Rule = {
    if (_body == null) {
      null
    } else {
      val ret = _body.toRule
      if (ret ne _body) {
        this.write(ret)
      }
      ret
    }
  }

  override def rhs = rBody.rhs

  override def lhs = rBody.lhs
}

