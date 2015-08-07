package degrel.primitives.rewriter.cell

import degrel.core.{Cell, Label, VertexHeader}
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

class Fin extends Rewriter {
  val finLabel = Label("fin")

  override def isMeta: Boolean = true

  override def rewrite(self: Driver, target: VertexHeader): RewriteResult = {
    if (target.isCell) {
      target.thru(Label.E.cellItem).find(_.label == finLabel) match {
        case Some(finV) => {
          val finValue = finV.thru(0).headOption.getOrElse(Cell())
          target.write(finValue)
          RewriteResult.write(target, finValue)
        }
        case None => RewriteResult.Nop
      }
    } else {
      RewriteResult.Nop
    }
  }

  override def pp(implicit opt: PrettyPrintOptions): String = {
    "<Built-in rule 'Fin'>"
  }
}
