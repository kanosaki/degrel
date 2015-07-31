package degrel.primitives.rewriter.lang

import degrel.core.{Cell, Label, VertexHeader}
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

/**
 * if(true, then: @X, else: @Y) -> X
 * if(false, then: @X, else: @Y) -> Y
 * if(true, then: X) -> X
 */
class If extends Rewriter {
  val ifLabel = Label("if")
  val thenLabel = Label("then")
  val elseLabel = Label("else")

  override def rewrite(self: Driver, target: VertexHeader): RewriteResult = {
    if (target.label != ifLabel) return RewriteResult.Nop
    val fullIfResult = for {
      pred <- target.thru(0).headOption
      thn <- target.thru(thenLabel).headOption
      els <- target.thru(elseLabel).headOption
    } yield pred.label match {
      case Label.V.vTrue => {
        RewriteResult.write(target, thn)
      }
      case Label.V.vFalse  => {
        RewriteResult.write(target, els)
      }
      case _ => RewriteResult.Nop
    }
    if (fullIfResult.isDefined) return fullIfResult.get

    val abbrIfResult = for {
      pred <- target.thru(0).headOption
      thn <- target.thru(thenLabel).headOption
    }  yield pred.label match {
      case Label.V.vTrue => {
        RewriteResult.write(target, thn)
      }
      case Label.V.vFalse => {
        RewriteResult.write(target, Cell())
      }
      case _ => RewriteResult.Nop
    }
    if (abbrIfResult.isDefined) return abbrIfResult.get

    RewriteResult.Nop
  }

  override def pp(implicit opt: PrettyPrintOptions): String = "<Built-in if rule>"
}
