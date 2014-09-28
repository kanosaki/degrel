package degrel.core

trait Element {
  def isIsomorphicTo(another: Element): Boolean = {
    operators.areIsomorphic(this, another)
  }

  def ==~(other: Element): Boolean = this.isIsomorphicTo(other)

  def =/~(other: Element): Boolean = !this.isIsomorphicTo(other)

  override def toString = this.reprRecursive(new Trajectory())

  def repr: String

  def reprRecursive(history: Trajectory): String
}

