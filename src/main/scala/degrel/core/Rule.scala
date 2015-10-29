package degrel.core

trait Rule extends Vertex {
  def rhs: Vertex = {
    this.thruSingle(Label.E.rhs)
  }

  def lhs: Vertex = {
    this.thruSingle(Label.E.lhs)
  }

  def preds: Iterable[Vertex] = {
    this.thru(Label.E.pred)
  }

  override def toRule: Rule = this

  override def asRule: Rule = this
}


object Rule {
  def apply(edges: Iterable[Edge]): Rule = {
    val (lhs, rhs, preds, pragmaEdges) = splitEdges(edges)
    new RuleVertexHeader(new RuleVertexBody(lhs, rhs, preds, pragmaEdges))
  }

  def splitEdges(edges: Iterable[Edge]): (Vertex, Vertex, Seq[Vertex], Seq[Edge]) = {
    val pragmaEdges = edges.filter(e => e.label != Label.E.rhs && e.label != Label.E.lhs && e.label != Label.E.pred)
    val rhs = edges.find(_.label == Label.E.rhs)
    val lhs = edges.find(_.label == Label.E.lhs)
    val preds = edges.filter(_.label == Label.E.pred).map(_.dst).toSeq
    (lhs.get.dst, rhs.get.dst, preds, pragmaEdges.toSeq)
  }
}
