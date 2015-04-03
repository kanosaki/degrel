package degrel.core

import degrel.core.utils.PrettyPrintOptions

trait Element {
  def isIsomorphicTo(another: Element): Boolean = {
    operators.areIsomorphic(this, another)
  }

  def ==~(other: Element): Boolean = this.isIsomorphicTo(other)

  def =/~(other: Element): Boolean = !this.isIsomorphicTo(other)

  def pp(implicit opt: PrettyPrintOptions = PrettyPrintOptions.default): String

  override def toString: String = this.pp
}

