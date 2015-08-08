package degrel.primitives.rewriter

import degrel.core.{Vertex, Label, VertexHeader}
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

abstract class BinOpRewriter[T <: Vertex] extends Rewriter {
  def label: Label

  def calc(lhs: Vertex, rhs: Vertex): Option[Vertex]

  override def rewrite(self: Driver, target: VertexHeader): RewriteResult = {
    if (target.label == this.label) {
      val result = for {
        lhs <- target.thru(Label.E.lhs).headOption
        rhs <- target.thru(Label.E.rhs).headOption
        calcRes <- calc(lhs, rhs)
      } yield calcRes
      result match {
        case Some(resVal) => {
          write(target, resVal)
        }
        case _ => nop
      }
    } else {
      nop
    }
  }

  override def pp(implicit opt: PrettyPrintOptions): String = s"<operator '${label.expr}>"
}
