package degrel.primitives.rewriter

import degrel.core._
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

import scala.reflect.runtime.universe._

abstract class ValueBinOp[TLhs: TypeTag, TRhs: TypeTag, TResult: TypeTag] extends Rewriter {
  def label: Label

  def calc(lhs: TLhs, rhs: TRhs): TResult

  override def rewrite(self: Driver, target: VertexHeader): RewriteResult = {
    if (target.label == this.label) {
      val result = for {
        lhs <- target.thru(Label.E.lhs).headOption
        lVal <- lhs.getValue[TLhs]
        rhs <- target.thru(Label.E.rhs).headOption
        rVal <- rhs.getValue[TRhs]
      } yield calc(lVal, rVal)
      result match {
        case Some(resVal) => {
          target.write(ValueVertex(resVal))
          RewriteResult.write(target, ValueVertex(resVal))
        }
        case _ => RewriteResult.Nop
      }
    } else {
      RewriteResult.Nop
    }
  }

  override def pp(implicit opt: PrettyPrintOptions): String = {
    s"<operator '${label.expr}'>"
  }
}
