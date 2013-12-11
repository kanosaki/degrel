package degrel.core

import degrel.engine._

case class Edge(label: Label, dst: Vertex) extends Product2[String, Vertex] with Element {
  def _1: String = label.expr

  def _2: Vertex = dst

  def repr: String = {
    s"${label.expr}:"
  }

  def reprRecursive = {
    s"${label.expr}:${dst.reprRecursive}"
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
    Edge(this.label, dst.build(context))
  }

  def isSameElement(other: Element): Boolean = other match {
    case e: Edge => (this.label == e.label) || (this.dst ==~ e.dst)
    case _ => false
  }

  def freeze: Edge = Edge(this.label, this.dst.freeze)
}

class EdgeEqualityAdapter(val target: Edge) {
  override def equals(other: Any) = other match {
    case e: Element => e ==~ target
    case eqAdapter: EdgeEqualityAdapter => eqAdapter.target ==~ this.target
    case _ => false
  }

  override def hashCode() = target.hashCode()
}

