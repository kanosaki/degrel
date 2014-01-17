package degrel.core


import degrel.rewriting.{MatchingContext, VertexMatching, BuildingContext}

trait Vertex extends Element {
  def edges(label: Label = Label.wildcard): Iterable[Edge]

  def attributes: Map[String, String]

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

  def asRoot: Graph = {
    this match {
      case g: Graph => g
      case _ => new Graph(this)
    }
  }

  /**
   * この頂点を基点にすべての接続を再帰的にトラバースし，VertexHeaderを除去して不変なグラフを新規に構築します．
   * @return 新規に構築された不変なグラフ
   */
  def freeze: Vertex = {
    this.freezeRecursive(new Footprints[Vertex]())
  }

  def freezeRecursive(footprints: Footprints[Vertex]): Vertex

  def deepCopy: Vertex = {
    this.copyRecursive(new Footprints[Vertex]())
  }

  def copyRecursive(footprints: Footprints[Vertex]): Vertex

  def shallowCopy: Vertex
}

object Vertex {
  def apply(label: String, edges: Iterable[Edge], attributes: Map[String, String] = Map()): Vertex = {
    val body = VertexBody(Label(label), attributes, edges.toSeq)
    new VertexHeader(body)
  }
}
