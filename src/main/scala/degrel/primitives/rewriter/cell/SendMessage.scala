package degrel.primitives.rewriter.cell

import degrel.core._
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.front.BinOp
import degrel.utils.PrettyPrintOptions

class SendMessage extends Rewriter {
  val sendMessageLabel = BinOp.MSG_SEND.toLabel

  override def rewrite(target: VertexHeader, parent: Driver): RewriteResult = {
    if (target.label == sendMessageLabel) {
      val lhs = target.thru(Label.E.lhs).headOption
      val rhs = target.thru(Label.E.rhs).headOption
      (lhs, rhs) match {
        case ((Some(l), Some(r))) if l.isCell => {
          val targetCell = l.unref[CellBody]
          targetCell.addRoot(r)
          target.write(targetCell)
          RewriteResult(done = true)
        }
        case _ => RewriteResult.NOP
      }
    } else {
      RewriteResult.NOP
    }
  }

  override def pp(implicit opt: PrettyPrintOptions): String = {
    "<Built-in rule 'SendMessage'>"
  }
}
