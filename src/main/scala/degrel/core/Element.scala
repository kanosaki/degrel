package degrel.core

trait Element {
  def isSameElement(other: Element): Boolean

  def isSame(other: Element): Boolean = {
    //(other eq this) || this.isSameElement(other)
    this.freeze == other.freeze
  }

  def ==~(other: Element): Boolean = this.isSame(other)

  def =/~(other: Element): Boolean = !this.isSame(other)

  override def toString = this.reprRecursive(new Trajectory())

  def freeze: Element

  def repr: String

  def reprRecursive(history: Trajectory): String
}

