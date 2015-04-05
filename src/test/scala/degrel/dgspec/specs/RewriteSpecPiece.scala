package degrel.dgspec.specs

import degrel.core.Cell
import degrel.dgspec.{NextPiece, SpecContext, SpecPiece}
import degrel.engine.Driver

case class RewriteSpecPiece() extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    ctx.root match {
      case ctxCell: Cell => {
        val pra = new Driver(ctxCell)
        pra.stepUntilStop()
        NextPiece.Continue
      }
      case _ => {
        NextPiece.Abort(s"Cell required at rewriting : got ${ctx.root}")
      }
    }
  }
}
