package degrel.core

import degrel.engine.rewriting.BuildingContext

trait VertexHeader extends Vertex {
  def body: VertexBody

  def write(v: Vertex): Unit

  def edges: Iterable[Edge] = body.edges

  def label: Label = body.label

  def attr(key: Label): Option[String] = body.attr(key)

  def build(context: BuildingContext): Vertex = body.build(context)

  def attributes: Map[Label, String] = body.attributes

  def id: ID = body.id

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
}

object VertexHeader {
  def apply(body: VertexBody): VertexHeader = {
    new LocalVertexHeader(body)
  }
}
