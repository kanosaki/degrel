package degrel.dgspec

import org.scalatest.FlatSpecLike

trait SpecPiece extends FlatSpecLike {

  def evaluate(ctx: SpecContext): NextPiece

}

trait NextPiece

object NextPiece {

  case object Continue extends NextPiece

  case class Abort(msg: String) extends NextPiece

}

