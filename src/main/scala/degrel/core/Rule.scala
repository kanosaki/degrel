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
  def apply(lhs: Vertex, rhs: Vertex, pragmaEdges: Seq[Edge] = Seq()): Rule = {
    new RuleVertexHeader(new RuleVertexBody(lhs, rhs, pragmaEdges))
  }

  def apply(edges: Iterable[Edge]): Rule = {
    val (lhs, rhs, pragmaEdges) = splitEdges(edges)
    new RuleVertexHeader(new RuleVertexBody(lhs, rhs, pragmaEdges))
  }

  def splitEdges(edges: Iterable[Edge]): (Vertex, Vertex, Seq[Edge]) = {
    val pragmaEdges = edges.filter(e => e.label != Label.E.rhs && e.label != Label.E.lhs)
    val rhs = edges.find(_.label == Label.E.rhs)
    val lhs = edges.find(_.label == Label.E.lhs)
    (lhs.get.dst, rhs.get.dst, pragmaEdges.toSeq)
  }
}
