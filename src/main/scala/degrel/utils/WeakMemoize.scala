package degrel.utils

import scala.collection.mutable

class WeakMemoize[-T, +R](f: T => R) extends (T => R) {

  private[this] val vals = mutable.WeakHashMap[T, R]()

  def apply(x: T): R = vals.getOrElseUpdate(x, f(x))
}

