package degrel.core

trait Element {
  def isIsomorphicTo(another: Element): Boolean = {
    operators.areIsomorphic(this, another)
  }

  def ==~(other: Element): Boolean = this.isIsomorphicTo(other)

  def =/~(other: Element): Boolean = !this.isIsomorphicTo(other)
}

