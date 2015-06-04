package degrel.dgspec.specs

import degrel.dgspec.{NextPiece, SpecContext, SpecPiece}

case object VoidSpecPiece extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    NextPiece.Continue
  }
}
