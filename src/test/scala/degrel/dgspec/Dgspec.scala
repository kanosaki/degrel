package degrel.dgspec

import java.nio.file.Path

import org.scalatest.{FlatSpecLike, Tag}

import scala.io.Source

trait Dgspec extends FlatSpecLike {
  def isIgnored = false

  def description: String

  def specTags: Array[Tag] = Array()

  def spec: SpecPiece

  def apply() = {
    val content = new SpecContext()
  }

  override def toString: String = {
    s"<Dgspec $description>"
  }
}
