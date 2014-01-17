package degrel.core

import degrel.rewriting._
import degrel.rewriting.EdgeBridge

case class Edge(label: Label, dst: Vertex) extends Product2[String, Vertex] with Element {
  def _1: String = label.expr

  def _2: Vertex = dst

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
    Edge(this.label, dst.build(context))
  }

  def isSameElement(other: Element): Boolean = other match {
    case e: Edge => (this.label == e.label) || (this.dst ==~ e.dst)
    case _ => false
  }

  def freeze: Edge = Edge(this.label, this.dst.freeze)

  def freezeRecursive(footprints: Footprints[Vertex]): Edge = {
    Edge(this.label, this.dst.freezeRecursive(footprints))
  }

  def copyRecursive(footprints: Footprints[Vertex]): Edge = {
    Edge(this.label, this.dst.copyRecursive(footprints))
  }

  def isReference: Boolean = this.label.expr == "_ref"
}

class EdgeEqualityAdapter(val target: Edge) {
  override def equals(other: Any) = other match {
    case e: Element => e ==~ target
    case eqAdapter: EdgeEqualityAdapter => eqAdapter.target ==~ this.target
    case _ => false
  }

  override def hashCode() = target.hashCode()
}

