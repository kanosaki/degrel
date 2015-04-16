package degrel.builtins.rewriter.math

import degrel.core._
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.front.BinOp

class Plus extends Rewriter {
  val plusLabel = BinOp.ADD.toLabel

  override def isSpawnsCells: Boolean = false

  override def rewrite(target: VertexHeader, parent: Cell): RewriteResult = {
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

  override def build(target: Vertex): Option[Vertex] = ???
}
