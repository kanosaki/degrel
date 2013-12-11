package degrel.core

import degrel.misc.PrettyPrintable

trait Element extends PrettyPrintable {
  def isSameElement(other: Element): Boolean

  def isSame(other: Element): Boolean = {
    (other eq this) || this.isSameElement(other)
  }

  def ==~(other: Element): Boolean = this.isSame(other)

  def =/~(other: Element): Boolean = !this.isSame(other)

  override def toString = this.reprRecursive

  def freeze: Element
}

trait Pattern {

}
