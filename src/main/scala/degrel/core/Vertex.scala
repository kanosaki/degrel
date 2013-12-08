package degrel.core


import degrel.engine.{VertexMatching, MatchingContext}

trait Vertex extends Element {
  def edges(label: Label = Label.wildcard): Iterable[Edge]

  def attr(key: String): Option[String]

  def hasAttr(key: String, value: String = null) = {
    value match {
      case null => this.attr(key).isDefined
      case v => this.attr(key) match {
        case None => false
        case Some(actualValue) => actualValue == v
      }
    }
  }

  def groupedEdges: Iterable[Iterable[Edge]]

  def label: Label

  def id: ID = {
    this.localID
  }

  protected def localID: ID = {
    LocalID(System.identityHashCode(this))
  }

  def hasEdge(label: Label = Label.wildcard): Boolean = {
    this.edges(label).size > 0
  }

  def matches(pattern: Vertex, context: MatchingContext = MatchingContext.empty): VertexMatching
}

object Vertex {
  def apply(label: String, edges: Iterable[Edge], attributes: Map[String, String] = Map()): Vertex = {
    val body = VertexBody(Label(label), attributes, edges.toSeq)
    new VertexHeader(body)
  }
}

