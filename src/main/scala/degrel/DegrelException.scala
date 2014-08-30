package degrel

class DegrelException(msg: String, cause: Throwable) extends Exception(msg, cause) {
  def this(msg: String) = this(msg, null)

  def this(cause: Throwable) = this(null, cause)

  def this() = this(null, null)
}
