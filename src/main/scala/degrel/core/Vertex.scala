package degrel.core


import degrel.rewriting.{BuildingContext, MatchingContext, VertexMatching}

trait Vertex extends Element with Comparable[Vertex] {
  def edges(label: Label = Label.wildcard): Iterable[Edge]

  def attributes: Map[String, String]

  def attr(key: String): Option[String]

  def id: ID

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


  def hasEdge(label: Label = Label.wildcard): Boolean = {
    this.edges(label).size > 0
  }

  def matches(pattern: Vertex, context: MatchingContext = MatchingContext.empty): VertexMatching = {
    Matcher(this).matches(pattern, context)
  }

  def build(context: BuildingContext): Vertex

  def isReference: Boolean = this.label == Label.reference

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

  def toGraph: Graph = {
    this match {
      case g: Graph => g
      case _ => new RawRootedGraph(this)
    }
  }

  def deepCopy: Vertex = {
    operators.duplicate(this)
  }

  def shallowCopy: Vertex

  def compareTo(o: Vertex): Int = {
    this.id.compareTo(o.id)
  }

  def version: VertexVersion = {
    this match {
      case vh: VertexHeader => VertexVersion(vh, vh.body)
      case _ => throw new Exception("VertexVersion is only available for VertexHeader.")
    }
  }
}

object Vertex {
  def apply(label: String,
            edges: Iterable[Edge],
            attributes: Map[String, String] = Map(),
            id: ID = ID.NA): Vertex = {
    val body = VertexBody(Label(label), attributes, edges.toSeq, id)
    new VertexHeader(body)
  }

  /**
   * Note: edgeInitに渡されるVertexはVertexHeaderのみなので注意
   * @param label
   * @param attributes
   * @param id
   * @param edgeInit
   * @return
   */
  def create(label: Label,
             attributes: Map[String, String] = Map(),
             id: ID = ID.NA)
            (edgeInit: Vertex => Iterable[Edge]): Vertex = {
    val vh = new VertexHeader(null)
    val edges = edgeInit(vh)
    val body = VertexBody(label, attributes, edges.toSeq, id)
    vh.write(body)
    vh
  }
}
