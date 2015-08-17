package degrel.primitives.rewriter

import degrel.core.{Label, Vertex}
import degrel.engine.rewriting.{RewritingTarget, RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

abstract class BinOpRewriter[T <: Vertex] extends Rewriter {
  def label: Label

  def calc(lhs: Vertex, rhs: Vertex): Option[Vertex]

  override def pattern: Vertex = parse(s"_ ${label.expr} _")

  override def rewrite(rt: RewritingTarget): RewriteResult = {
    val target = rt.target
    if (target.label == this.label) {
      val result = for {
        lhs <- target.thru(Label.E.lhs).headOption
        rhs <- target.thru(Label.E.rhs).headOption
        calcRes <- calc(lhs, rhs)
      } yield calcRes
      result match {
        case Some(resVal) => {
          write(rt, resVal)
        }
        case _ => nop
      }
    } else {
      nop
    }
  }

  override def pp(implicit opt: PrettyPrintOptions): String = s"<operator '${label.expr}>"
}
