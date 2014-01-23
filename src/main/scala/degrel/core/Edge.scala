package degrel.core

import degrel.rewriting._
import degrel.rewriting.EdgeBridge

object Edge {
  def apply(src: => Vertex, label: Label, dst: => Vertex): Edge = {
    new Edge(src, label, dst)
  }
}

class Edge(_src: => Vertex, _label: Label, _dst: => Vertex) extends Product2[String, Vertex] with Element {
  def label = _label

  lazy val dst = _dst

  private var __sourceVertex: Vertex = null

  def src: Vertex = {
    __sourceVertex match {
      case null => {
        __sourceVertex = _src
        if (__sourceVertex == null) throw new AssertionError("Source of an edge cannot be null")
        __sourceVertex
      }
      case _ => __sourceVertex
    }
  }


  def src_=(v: Vertex) = {
    if (v == null) {
      throw new NullPointerException("Argument cannot be null")
    }
    if (__sourceVertex != null && (__sourceVertex eq v)) {
      throw new AssertionError("Souce can set only once.")
    }
    __sourceVertex = v
  }

  def _1: String = label.expr

  def _2: Vertex = dst

  def canEqual(that: Any): Boolean = that match {
    case e: Edge => true
    case _ => false
  }

  override def equals(other: Any) = other match {
    //case e: Edge => e.label == this.label && e.dst.id == this.dst.id && this.src.id == e.src.id
    case e: Edge => e.label == this.label && this.dst == e.dst
    case _ => false
  }

  override def hashCode = {
    val prime = 139
    var result = 1
    result = prime * result + label.hashCode()
    //result = prime * result + src.id.hashCode()
    //result = prime * result + dst.id.hashCode()
    result
  }

  def repr: String = {
    s"${label.expr}:"
  }

  def reprRecursive(history: Trajectory) = {
    s"${label.expr}:${dst.reprRecursive(history)}"
  }

  def matches(pattern: Edge, context: MatchingContext): EdgeMatching = {
    if (this.label.matches(pattern.label))
      dst.matches(pattern.dst, context) match {
        case NoMatching => NoMatching
        case v: VertexMatching => MonoEdgeMatching(EdgeBridge(pattern, this), v)
      }
    else
      NoMatching
  }

  def build(context: BuildingContext): Edge = {
    Edge(this.src, this.label, dst.build(context))
  }

  def isSameElement(other: Element): Boolean = other match {
    case e: Edge => (this.label == e.label) || operators.areSame(this.dst, e.dst)
    case _ => false
  }

  def freeze: Edge = Edge(this.src, this.label, this.dst.freeze)

  def isReference: Boolean = this.label.expr == "_ref"

  def duplicate(): Edge = {
    Edge(null, this.label, this.dst)
  }

}

class EdgeEqualityAdapter(val target: Edge) {
  override def equals(other: Any) = other match {
    case e: Element => e ==~ target
    case eqAdapter: EdgeEqualityAdapter => eqAdapter.target ==~ this.target
    case _ => false
  }

  override def hashCode() = target.hashCode()
}

