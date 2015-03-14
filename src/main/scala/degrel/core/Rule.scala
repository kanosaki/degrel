package degrel.core

trait Rule extends Vertex {
  def rhs: Vertex

  def lhs: Vertex
}


object Rule {
  def apply(lhs: Vertex, rhs: Vertex) = {
    new RuleVertexHeader(lhs, rhs)
  }
}
