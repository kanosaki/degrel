package degrel.dgspec.specs

import degrel.Logger
import degrel.dgspec.{NextPiece, SpecContext, SpecPiece}
import degrel.utils.PrettyPrintOptions


/**
 * 現在のコンテキストへ値をセットします
 */
case class InspectSpecPiece() extends SpecPiece with Logger {
  implicit val ppOpt = PrettyPrintOptions(showAllId = true, multiLine = true)

  override def evaluate(ctx: SpecContext): NextPiece = {
    logger.info(s"ROOT:\n${ctx.root.pp}")
    NextPiece.Continue
  }
}

object InspectSpecPiece {
}

