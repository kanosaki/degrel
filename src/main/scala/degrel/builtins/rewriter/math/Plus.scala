package degrel.builtins.rewriter.math

import degrel.core._
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.front.BinOp
import degrel.utils.PrettyPrintOptions

class Plus extends Rewriter {
  val plusLabel = BinOp.ADD.toLabel

  override def rewrite(target: VertexHeader, parent: Driver): RewriteResult = {
    if (target.label == plusLabel) {
      val result = for {
        lhs <- target.thru(Label.E.lhs).headOption
        lVal <- lhs.getValue[Int]
        rhs <- target.thru(Label.E.rhs).headOption
        rVal <- rhs.getValue[Int]
      } yield rVal + lVal
      result match {
        case Some(resVal) => {
          target.write(ValueVertex(resVal))
          RewriteResult(done = true)
        }
        case _ => RewriteResult.NOP
      }
    } else {
      RewriteResult.NOP
    }
  }

  override def pp(implicit opt: PrettyPrintOptions): String = {
    "<Built-in rule 'Plus'>"
  }
}
