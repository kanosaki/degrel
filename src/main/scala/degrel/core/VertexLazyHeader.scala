package degrel.core

import degrel.engine._


class VertexLazyHeader(f: Unit => VertexBody) extends Vertex {
  lazy val body = f()

  def edges(label: Label): Iterable[Edge] = body.edges(label)

  def groupedEdges: Iterable[Iterable[Edge]] = body.groupedEdges

  def label: Label = body.label

  def repr: String = {
    s"<${body.repr}>"
  }

  def reprRecursive = {
    s"<${body.reprRecursive}>"
  }

  def isSameElement(other: Element): Boolean = other match {
    case vh: VertexLazyHeader => this.body ==~ vh.body
    case _ => false
  }


  def attr(key: String): Option[String] = body.attr(key)

  def build(context: BuildingContext): Vertex = body.build(context)

  def freeze: VertexBody = {
    this.body.freeze
  }
}
