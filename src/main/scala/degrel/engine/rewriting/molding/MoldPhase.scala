package degrel.engine.rewriting.molding

trait MoldPhase {
  def next: Option[MoldPhase]
}

object MoldPhase {
  case object Scan extends MoldPhase {
    val next = Some(MoldPhase.Mold)
  }
  case object Mold extends MoldPhase {
    val next = Some(MoldPhase.After)
  }

  case object After extends MoldPhase {
    val next = None
  }
}
