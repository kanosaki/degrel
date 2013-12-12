package degrel.core


import degrel.rewriting.{MatchingContext, VertexMatching, BuildingContext}

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

  def matches(pattern: Vertex, context: MatchingContext = MatchingContext.empty): VertexMatching = {
    Matcher(this).matches(pattern, context)
  }

  def build(context: BuildingContext): Vertex

  def isReference: Boolean = this.label == Label.reference

  // TODO: Move other location
  def referenceTarget: Vertex = {
    assert(this.label == Label.reference)
    val refEdges = this.edges("_ref")
    assert(refEdges.size == 1)
    refEdges.head.dst
  }

  def thru(label: Label): Vertex = {
    val candidates = this.edges(label)
    candidates.size match {
      case 1 => candidates.head.dst
      case 0 => throw new Exception("No edge found.")
      case _ => throw new Exception("Too many edge found.")
    }
  }

  def asRule: Rule = {
    assert(this.label.expr == "->")
    val rhs = this.thru("_rhs")
    val lhs = this.thru("_lhs")
    Rule(lhs, rhs)
  }

  def freeze: Vertex
}

object Vertex {
  def apply(label: String, edges: Iterable[Edge], attributes: Map[String, String] = Map()): Vertex = {
    val body = new VertexBody(Label(label), attributes, edges.toSeq)
    new VertexEagerHeader(body)
  }
}
