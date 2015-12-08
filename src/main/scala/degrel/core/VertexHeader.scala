package degrel.core

import degrel.utils.collection.mutable.WeakLinearSet

import scala.reflect.runtime.universe.TypeTag

abstract class VertexHeader(var id: ID = ID.NA) extends Vertex {
  protected val reverse = WeakLinearSet[VertexBody]()
  protected var logicalVersion = 0

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
    val prevID = this.id
    id = id.withOwner(owner)
    //println(s"TransferOwner ${this.pp} $prevID --(${owner.id})--> $id")
  }

  def updateID(newID: ID): Unit = {
    //println(s"WriteOwner ${this.pp} $id --> $newID")
    this.id = newID
  }

  def fingerprintCache: Long = {
    body.fingerprintCache
  }

  def fingerprintCache_=(fp: Long): Unit = {
    body.fingerprintCache = fp
  }

  def pin: VertexPin = {
    VertexPin(this.id, logicalVersion)
  }
}

object VertexHeader {
  def apply(body: VertexBody, initID: ID = ID.nextLocalID()): VertexHeader = {
    new LocalVertexHeader(body, initID)
  }
}
