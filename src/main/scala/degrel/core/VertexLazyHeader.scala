package degrel.core

import degrel.engine._


class VertexLazyHeader(f: Unit => VertexBody) extends Vertex {
  lazy val body = f()

  override def equals(other: Any) = other match {
    case vh: VertexLazyHeader => (vh eq this) || vh.body == this.body
    case vb: VertexBody => vb == this.body
    case _ => false
  }

  override def hashCode = {
    System.identityHashCode(this)
  }

  def edges(label: Label): Iterable[Edge] = body.edges(label)

  def groupedEdges: Iterable[Iterable[Edge]] = body.groupedEdges

  def label: Label = body.label

  override def toString: String = this.repr

  def repr: String = {
    s"<${body.repr}>"
  }

  def reprRecursive = {
    s"<${body.reprRecursive}>"
  }


  def attr(key: String): Option[String] = body.attr(key)

  def build(context: BuildingContext): Vertex = body.build(context)
}
