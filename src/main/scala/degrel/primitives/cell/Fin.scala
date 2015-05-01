package degrel.primitives.cell

import degrel.core.{Cell, Label, Vertex, VertexHeader}
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

class Fin extends Rewriter {
  val finLabel = Label("fin")

  override def isMeta: Boolean = true

  override def rewrite(target: VertexHeader, parent: Driver): RewriteResult = {
    if (target.label == finLabel) {
      val lhs = target.thru(Label.E.lhs).headOption
      val rhs = target.thru(Label.E.rhs).headOption
      (lhs, rhs) match {
        case ((Some(l), Some(r))) if l.isCell => {
          val targetCell = l.asCell
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
    "<Built-in rule 'Fin'>"
  }
}
