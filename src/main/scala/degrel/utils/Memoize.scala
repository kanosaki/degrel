package degrel.utils

class Memoize[-T, +R](f: T => R) extends (T => R) {

  import scala.collection.mutable

  private[this] val vals = mutable.Map.empty[T, R]

  def apply(x: T): R = vals getOrElseUpdate(x, f(x))
}

object Memoize {
  def apply[T, R](f: T => R) = {
    new Memoize(f)
  }

  def weak[T, R](f: T => R) = {
    new WeakMemoize(f)
  }
}

