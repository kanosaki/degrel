package degrel.control

trait Verbosity extends Ordered[Verbosity] {
  def importance: Int

  override def compare(that: Verbosity): Int = {
    if (that == null) throw new NullPointerException()
    Integer.compare(this.importance, that.importance)
  }
}

object Verbosity {
  def default = Warning

  case object Debug extends Verbosity {
    override def importance: Int = 10
  }

  case object Info extends Verbosity {
    override def importance: Int = 20
  }

  case object Warning extends Verbosity {
    override def importance: Int = 30
  }

  case object Error extends Verbosity {
    override def importance: Int = 40
  }

}
