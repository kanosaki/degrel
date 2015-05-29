package degrel

trait DegrelException extends Throwable {
  def msg: String
  def cause: Throwable

  override def getMessage: String = this.msg

  override def getCause: Throwable = this.cause
}

class DefaultDegrelException(val msg: String, val cause: Throwable) extends Exception with DegrelException {
}

object DegrelException {
  def apply(msg: String, cause: Throwable = null): DegrelException = {
    new DefaultDegrelException(msg, cause)
  }
}
