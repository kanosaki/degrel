package degrel.core

import degrel.engine._

case class Edge(label: Label, dst: Vertex) extends Product2[String, Vertex] with Element {
  def _1: String = label.expr

  def _2: Vertex = dst

  override def toString: String = {
    s"${label.expr}: $dst"
  }

  def matches(pattern: Edge, context: MatchingContext): MatchedEdge = {
    if (this.label.matches(pattern.label))
      dst.matches(pattern.dst, context) match {
        case NoMatch => NoMatch
        case v: MatchedVertex => SingleMatchedEdge(EdgeBind(pattern, this), v)
      }
    else
      NoMatch
  }
}

