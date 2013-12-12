package degrel.core

import degrel.engine._


class VertexHeader(f: Unit => VertexBody) extends Vertex {
  private var _body: VertexBody = null
  def body: VertexBody = {
    if(_body == null) {
      _body = f()
    }
    _body
  }

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
    case vh: VertexHeader => this.body ==~ vh.body
    case _ => false
  }

  def attr(key: String): Option[String] = body.attr(key)

  def build(context: BuildingContext): Vertex = body.build(context)

  def freeze: VertexBody = {
    this.body.freeze
  }

  def write(v: Vertex) = v match {
    case vb: VertexBody => _body = vb
    case vh: VertexHeader => _body = vh.body
  }
}
