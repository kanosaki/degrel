package degrel.core

import degrel.utils.PrettyPrintable

trait Element extends PrettyPrintable {
  def ==~(other: Element): Boolean = this.isIsomorphicTo(other)

  def isIsomorphicTo(another: Element): Boolean = {
    operators.areIsomorphic(this, another)
  }

  def =/~(other: Element): Boolean = !this.isIsomorphicTo(other)

  override def toString: String = this.pp
}

