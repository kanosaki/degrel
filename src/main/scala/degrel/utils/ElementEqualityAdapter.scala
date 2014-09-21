package degrel.utils

import degrel.core.Element

class ElementEqualityAdapter(val target: Element) {
  override def equals(other: Any) = other match {
    case v: ElementEqualityAdapter => v.target ==~ this.target
    case v: Element => target ==~ v
  }

  override def hashCode() = target.hashCode()
}
