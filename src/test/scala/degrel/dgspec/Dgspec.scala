package degrel.dgspec

import org.scalatest.{FlatSpecLike, Tag}

trait Dgspec extends FlatSpecLike {
  def isIgnored = false

  def description: String

  def specTags: Array[Tag] = Array()

  def spec: SpecPiece

  def apply(): Unit

  override def toString: String = {
    s"<Dgspec $description>"
  }
}
