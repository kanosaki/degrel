package degrel.core

import degrel.utils.collection.mutable.WeakLinearSet

import scala.reflect.runtime.universe.TypeTag

trait VertexHeader extends Vertex {
  protected val reverse = WeakLinearSet[VertexBody]()

  def body: VertexBody

  def write(v: Vertex): Unit

  def edges: Iterable[Edge] = body.edges

  def label: Label = body.label

  def attr(key: Label): Option[String] = body.attr(key)

  def attributes: Map[Label, String] = body.attributes

  override def isValue: Boolean = body.isValue

  override def isHeader = true

  override def getValue[T: TypeTag]: Option[T] = body.getValue[T]

  def addRevrseNeighbor(vb: VertexBody) = {
    reverse += vb
  }

  def reverseNeighbors: Iterable[VertexBody] = {
    reverse
  }


  val id: ID = ID.autoAssign
}

object VertexHeader {
  def apply(body: VertexBody): VertexHeader = {
    new LocalVertexHeader(body)
  }
}
