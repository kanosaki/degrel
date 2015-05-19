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
  val trueLabel = Label("true")
  val falseLabel = Label("false")
  val thenLabel = Label("then")
  val elseLabel = Label("else")

  override def rewrite(target: VertexHeader, parent: Driver): RewriteResult = {
    if (target.label != ifLabel) return RewriteResult.NOP
    val fullIfResult = for {
      pred <- target.thru(0).headOption
      thn <- target.thru(thenLabel).headOption
      els <- target.thru(elseLabel).headOption
    } yield pred.label match {
      case `trueLabel` => {
        target.write(thn)
        RewriteResult(done = true)
      }
      case `falseLabel`  => {
        target.write(els)
        RewriteResult(done = true)
      }
      case _ => RewriteResult.NOP
    }
    if (fullIfResult.isDefined) return fullIfResult.get

    val abbrIfResult = for {
      pred <- target.thru(0).headOption
      thn <- target.thru(thenLabel).headOption
    }  yield pred.label match {
      case `trueLabel` => {
        target.write(thn)
        RewriteResult(done = true)
      }
      case `falseLabel`  => {
        target.write(Cell())
        RewriteResult(done = true)
      }
      case _ => RewriteResult.NOP
    }
    if (abbrIfResult.isDefined) return abbrIfResult.get

    RewriteResult.NOP
  }

  override def pp(implicit opt: PrettyPrintOptions): String = "<Built-in if rule>"
}
