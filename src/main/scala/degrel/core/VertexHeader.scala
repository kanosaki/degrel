package degrel.core

import degrel.engine.rewriting.BuildingContext


class VertexHeader(f: VertexBody) extends Vertex {
  private var _body: VertexBody = f

  def edges: Iterable[Edge] = body.edges

  def body: VertexBody = {
    _body
  }

  def label: Label = body.label

  def repr: String = {
    s"<${body.repr}>"
  }

  def reprRecursive(trajectory: Trajectory) = {
    trajectory.walk(this) {
      case Unvisited(nextHistory) => {
        s"<${body.reprRecursive(nextHistory)}>"
      }
      case Visited(_) => {
        s"<${body.repr}(..)>"
      }
    }
  }

  def shallowCopy: Vertex = {
    new VertexHeader(this.body)
  }

  def isSameElement(other: Element): Boolean = other match {
    case vh: VertexHeader => this.body ==~ vh.body
    case _ => false
  }

  def attr(key: Label): Option[String] = body.attr(key)

  def build(context: BuildingContext): Vertex = body.build(context)


  def write(v: Vertex) = v match {
    case vb: VertexBody => _body = vb
    case vh: VertexHeader => _body = vh.body
  }

  def attributes: Map[Label, String] = body.attributes

  def id: ID = body.id
}
