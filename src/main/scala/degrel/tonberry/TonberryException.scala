package degrel.tonberry

import degrel.DegrelException

class TonberryException(msg: String, cause: Throwable) extends DegrelException(msg, cause) {
  def this(msg: String) = this(msg, null)
  def this(cause: Throwable) = this(null, cause)
  def this() = this(null, null)
}
