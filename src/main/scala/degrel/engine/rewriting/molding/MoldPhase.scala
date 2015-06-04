package degrel.engine.rewriting.molding

trait MoldPhase extends Ordered[MoldPhase] {
  def next: Option[MoldPhase]

  def phase: Int

  override def compare(that: MoldPhase): Int = {
    if (that == null) throw new NullPointerException()
    Integer.compare(this.phase, that.phase)
  }
}

object MoldPhase {

  case object Initial extends MoldPhase {
    val next = Some(MoldPhase.Scan)
    override val phase = 0
  }

  case object Scan extends MoldPhase {
    val next = Some(MoldPhase.Mold)
    override val phase = 1
  }

  case object Mold extends MoldPhase {
    val next = Some(MoldPhase.After)
    override val phase = 2
  }

  case object After extends MoldPhase {
    val next = None
    override val phase = 3
  }

}
