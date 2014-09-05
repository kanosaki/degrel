package degrel

import scala.language.implicitConversions

package object utils {
  val support = SupportUtils

  implicit def toRunnable[F](f: => F) = new Runnable() {
    def run() {
      f
    }
  }
}
