package degrel.core

import degrel.core.utils.PrettyPrintOptions
import degrel.engine.rewriting.matching._
import degrel.engine.rewriting.molding.MoldingContext
import degrel.engine.rewriting.{EdgeBridge, _}

object Edge {
  def apply(src: Vertex, label: Label, dst: Vertex): Edge = {
    new Edge(src, label, dst)
  }
}

class Edge(private var _src: Vertex, _label: Label, _dst: Vertex)
  extends Product2[String, Vertex]
  with Element
  with Comparable[Edge] {
  def dst = _dst
  def src = _src

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

  def matches(pattern: Edge, context: MatchingContext): EdgeMatching = {
    if (this.label.matches(pattern.label))
      dst.matches(pattern.dst, context) match {
        case NoMatching => NoMatching
        case v: VertexMatching => MonoEdgeMatching(EdgeBridge(pattern, this), v)
      }
    else
      NoMatching
  }

  def label = _label

  def src_=(v: Vertex) = {
    if (v == null) {
      throw new NullPointerException("Argument cannot be null")
    }
    _src = v
  }

  def isReference: Boolean = this.label == Label.E.ref

  def isOthers: Boolean = this.label == Label.E.others

  def isMeta: Boolean = this.label.isMeta

  def shallowCopy(): Edge = {
    Edge(this.src, this.label, this.dst)
  }

  override def compareTo(o: Edge): Int = {
    this.label.compareTo(o.label)
  }

  override def pp(implicit opt: PrettyPrintOptions = PrettyPrintOptions.default): String = {
    s"${label.expr}: ${utils.pp(dst)}"
  }
}
