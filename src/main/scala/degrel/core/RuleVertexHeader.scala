package degrel.core

class RuleVertexHeader(bdy: VertexBody, override val id: ID = ID.autoAssign)
  extends LocalVertexHeader(bdy, id) with Rule {

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

