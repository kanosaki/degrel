package degrel.primitives.rewriter.lang

import degrel.core.{Cell, Label, Vertex}
import degrel.engine.rewriting.{RewritingTarget, RewriteResult, Rewriter, RewritingTarget$}
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


  override def pattern: Vertex = parse("if(then: _)")

  override def rewrite(rt: RewritingTarget): RewriteResult = {
    val target = rt.target
    if (target.label != ifLabel) return nop
    val fullIfResult = for {
      pred <- target.thru(0).headOption
      thn <- target.thru(thenLabel).headOption
      els <- target.thru(elseLabel).headOption
    } yield pred.label match {
      case Label.V.vTrue => {
        write(rt, thn)
      }
      case Label.V.vFalse  => {
        write(rt, els)
      }
      case _ => nop
    }
    if (fullIfResult.isDefined) return fullIfResult.get

    val abbrIfResult = for {
      pred <- target.thru(0).headOption
      thn <- target.thru(thenLabel).headOption
    }  yield pred.label match {
      case Label.V.vTrue => {
        write(rt, thn)
      }
      case Label.V.vFalse => {
        write(rt, Cell())
      }
      case _ => nop
    }
    if (abbrIfResult.isDefined) return abbrIfResult.get

    nop
  }

  override def pp(implicit opt: PrettyPrintOptions): String = "<Built-in if rule>"
}
