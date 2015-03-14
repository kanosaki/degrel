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

  val id: ID = ID.autoAssign
}

object VertexHeader {
  def apply(body: VertexBody): VertexHeader = {
    new LocalVertexHeader(body)
  }
}
