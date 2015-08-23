package degrel.primitives.rewriter.cell

import degrel.core._
import degrel.engine.rewriting.{RewritingTarget, RewriteResult, Rewriter, RewritingTarget$}
import degrel.front.BinOp
import degrel.utils.PrettyPrintOptions

class SendMessage extends Rewriter {
  val sendMessageLabel = BinOp.MSG_SEND.toLabel

  override def rewrite(rt: RewritingTarget): RewriteResult = {
    val target = rt.target
    if (target.label == sendMessageLabel) {
      val lhs = target.thru(Label.E.lhs).headOption
      val rhs = target.thru(Label.E.rhs).headOption
      (lhs, rhs) match {
        case ((Some(l), Some(r))) if l.isCell => {
          val targetCell = l.unhead[CellBody]
          multi(addRoot(targetCell, r),
                write(rt, targetCell))
        }
        case _ => nop
      }
    } else {
      nop
    }
  }


  override def pattern: Vertex = parse("_ ! _")

  override def pp(implicit opt: PrettyPrintOptions): String = {
    "<Built-in rule 'SendMessage'>"
  }
}
