package degrel.dgspec.specs

import degrel.dgspec.{NextPiece, SpecContext, SpecPiece}
import degrel.engine.Praparat

case class RewriteSpecPiece() extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    val pra = new Praparat(ctx.root.asCell)
    pra.stepUntilStop()
    NextPiece.Continue
  }
}
