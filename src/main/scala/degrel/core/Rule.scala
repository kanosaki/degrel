package degrel.core

trait Rule extends Vertex {
  def rhs: Vertex

  def lhs: Vertex

  override def toRule: Rule = this
}


object Rule {
  def apply(lhs: Vertex, rhs: Vertex) = {
    new RuleVertexHeader(lhs, rhs)
  }
}
