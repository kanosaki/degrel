package degrel.core

trait Rule extends Vertex {
  def rhs: Vertex = {
    this.thruSingle(Label.E.lhs)
  }

  def lhs: Vertex = {
    this.thruSingle(Label.E.rhs)
  }

  override def toRule: Rule = this

  override def asRule: Rule = this
}


object Rule {
  def apply(lhs: Vertex, rhs: Vertex): Rule = {
    new RuleVertexHeader(new RuleVertexBody(lhs, rhs))
  }

  def apply(v: VertexBody): Rule = {
    new RuleVertexHeader(v)
  }
}
