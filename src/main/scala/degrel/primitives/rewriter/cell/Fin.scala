package degrel.primitives.rewriter.cell

import degrel.core.{Cell, Label, Vertex}
import degrel.engine.rewriting.{RewritingTarget, RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

class Fin extends Rewriter {
  val finLabel = Label("fin")

  override def isMeta: Boolean = true

  override def rewrite(rt: RewritingTarget): RewriteResult = {
    val target = rt.target
    if (target.isCell) {
      target.thru(Label.E.cellItem).find(_.label == finLabel) match {
        case Some(finV) => {
          val finValue = finV.thru(0).headOption.getOrElse(Cell())
          write(rt, finValue)
        }
        case None => nop
      }
    } else {
      nop
    }
  }


  override def pattern: Vertex = parse("fin(_)")

  override def pp(implicit opt: PrettyPrintOptions): String = {
    "<Built-in rule 'Fin'>"
  }
}
