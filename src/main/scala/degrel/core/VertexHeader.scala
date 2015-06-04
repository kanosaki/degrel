package degrel.core

import scala.reflect.runtime.universe.TypeTag

trait VertexHeader extends Vertex {
  def body: VertexBody

  def write(v: Vertex): Unit

  def edges: Iterable[Edge] = body.edges

  def label: Label = body.label

  def attr(key: Label): Option[String] = body.attr(key)

  def attributes: Map[Label, String] = body.attributes

  override def isValue: Boolean = body.isValue

  override def getValue[T: TypeTag]: Option[T] = body.getValue[T]

  val id: ID = ID.autoAssign

  override def asCell: Cell = {
    if (this.body.isCell) {
      this.asInstanceOf[Cell]
    } else {
      super.asCell
    }
  }
}

object VertexHeader {
  def apply(body: VertexBody): VertexHeader = {
    new LocalVertexHeader(body)
  }
}
