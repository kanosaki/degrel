package degrel

import scala.language.{implicitConversions, reflectiveCalls}

package object utils {
  val support = SupportUtils

  implicit def toRunnable[F](f: => F) = new Runnable() {
    def run() {
      f
    }
  }

  // From http://www.ne.jp/asahi/hishidama/home/tech/scala/sample/using.html
  def usingOpt[A <: {def close()}, B](resource: A)(func: A => B): Option[B] =
    try {
      Some(func(resource))
    } catch {
      case e: Exception => e.printStackTrace()
        None
    } finally {
      if (resource != null) resource.close()
    }

  def using[A <: {def close()}, B](resource: A)(func: A => B): B =
    try {
      func(resource)
    } finally {
      if (resource != null) resource.close()
    }
}
