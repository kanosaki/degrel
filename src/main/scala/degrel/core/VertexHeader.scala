package degrel.core

import degrel.engine.{MatchedVertex, MatchedEdge, MatchedGraph, MatchingContext}

class VertexHeader(protected var body: VertexBody) extends Vertex {

  override def equals(other: Any) = other match {
    case vh: VertexHeader => vh.body == this.body
    case vb: VertexBody => vb == this.body
    case _ => false
  }

  def edges(label: Label): Iterable[Edge] = body.edges(label)

  def label : Label = body.label

  override def toString: String = {
    s"<$id#$body>"
  }

  def matches(pattern: Vertex, context: MatchingContext): MatchedVertex = {
    // capture two vertex body
    val targetVertex = this.body
    val patternVertex = pattern match {
      case v: VertexHeader => v.body
      case _ => throw new Exception("VertexHeader required")
    }
    targetVertex.matches(patternVertex, context)
  }
}
