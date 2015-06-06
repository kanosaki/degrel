package degrel.utils.tonberry

import degrel.DefaultDegrelException

class TonberryException(msg: String, cause: Throwable) extends DefaultDegrelException(msg, cause) {
  def this(msg: String) = this(msg, null)

  def this(cause: Throwable) = this(null, cause)

  def this() = this(null, null)
}
