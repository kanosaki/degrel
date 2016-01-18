package degrel.dgspec.specs

import degrel.core.Cell
import degrel.dgspec.{NextPiece, SpecContext, SpecPiece}
import degrel.engine.LocalDriver

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
 * 現在のコンテキストで書き換えを実行します
 */
case class RewriteSpecPiece() extends SpecPiece {
  implicit val context = ExecutionContext.Implicits.global

  override def evaluate(ctx: SpecContext): NextPiece = {
    ctx.root match {
      case ctxCell: Cell => {
        val pra = LocalDriver(ctxCell)
        Await.result(pra.start(), 5.seconds)
        NextPiece.Continue
      }
      case _ => {
        NextPiece.Abort(s"Cell required at rewriting : got ${ctx.root}")
      }
    }
  }
}

object RewriteSpecPiece {
  val DEFAULT_MAX_REWRITE_COUNT = 100
}
