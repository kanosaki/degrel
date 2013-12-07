package degrel.core


import degrel.engine.{VertexMatching, MatchingContext}

trait Vertex extends Element {
  def edges(label: Label = Label.wildcard): Iterable[Edge]

  def groupedEdges: Iterable[Iterable[Edge]]

  def label: Label

  def id: ID = {
    this.localID
  }

  protected def localID: ID = {
    LocalID(System.identityHashCode(this))
  }

  def matches(pattern: Vertex, context: MatchingContext = MatchingContext.empty): VertexMatching
}

object Vertex {
  def apply(label: String, edges: Iterable[Edge]): Vertex = {
    val body = VertexBody(Label(label), edges.toSeq)
    new VertexHeader(body)
  }
}

