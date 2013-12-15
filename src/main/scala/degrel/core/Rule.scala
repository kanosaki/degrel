package degrel.core

trait Rule extends Vertex {
  def rhs: Vertex

  def lhs: Vertex

  override def reprRecursive = {
    s"${lhs.reprRecursive} -> ${rhs.reprRecursive}"
  }
}

case class RuleVertexBody(_lhs: Vertex, _rhs: Vertex)
  extends VertexBody("->",
                      Map(),
                      Stream(Edge("_lhs", _lhs),
                              Edge("_rhs", _rhs)))
          with Rule {
  def rhs = _rhs

  def lhs = _lhs

  override def reprRecursive = super[Rule].reprRecursive

  override def freeze = {
    RuleVertexBody(lhs.freeze, rhs.freeze)
  }
}

class RuleVertexHeader(_lhs: Vertex, _rhs: Vertex)
  extends VertexHeader(_ => RuleVertexBody(_lhs, _rhs)) with Rule {

  private def rBody = this.body.asInstanceOf[RuleVertexBody]

  def rhs = rBody.rhs

  def lhs = rBody.lhs

  override def reprRecursive = super[Rule].reprRecursive
}

object Rule {
  def apply(lhs: Vertex, rhs: Vertex) = {
    new RuleVertexHeader(lhs, rhs)
  }
}
