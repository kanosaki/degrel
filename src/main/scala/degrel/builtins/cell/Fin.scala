package degrel.builtins.cell

import degrel.core.{Cell, Label, Vertex, VertexHeader}
import degrel.engine.rewriting.{RewriteResult, Rewriter}

class Fin extends Rewriter {
  val finLabel = Label("fin")

  /**
   * Cellを扱いますが生成は行わないため`false`
   * @inheritdoc
   */
  override def isSpawnsCells: Boolean = true

  override def build(target: Vertex): Option[Vertex] = ???

  override def rewrite(target: VertexHeader, parent: Cell): RewriteResult = {
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
}
