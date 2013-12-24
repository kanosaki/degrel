package degrel.utils

import scala.language.implicitConversions

object SupportUtils {
  class NumberExtension(num: Int) {
    def hex(len: Integer = -1, prefix: String = "") = {
      val hexstr = Integer.toHexString(num)
      if(len > hexstr.size) {
        prefix + ("0" * (len - hexstr.size)) + hexstr
      } else {
        prefix + hexstr
      }
    }
  }

  implicit def supportutil_numberExtensions(num: Int) = new NumberExtension(num)
}
