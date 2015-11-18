package degrel.core

import degrel.utils.collection.mutable.WeakLinearSet

import scala.reflect.runtime.universe.TypeTag

abstract class VertexHeader(var id: ID = ID.NA) extends Vertex {
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

  override def tryOwn(owner: Vertex): Boolean = {
    if (this.id.canOwnBy(owner)) {
      this.transferOwner(owner)
      true
    } else {
      false
    }
  }


  override def transferOwner(owner: Vertex): Unit = {
    id = id.withOwner(owner)
  }

  def fingerprintCache: Long = {
    body.fingerprintCache
  }

  def fingerprintCache_=(fp: Long): Unit = {
    body.fingerprintCache = fp
  }
}

object VertexHeader {
  def apply(body: VertexBody, initID: ID = ID.nextLocalVertexID()): VertexHeader = {
    new LocalVertexHeader(body, initID)
  }
}
