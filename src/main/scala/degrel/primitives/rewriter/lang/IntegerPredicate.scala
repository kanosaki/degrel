package degrel.primitives.rewriter.lang

import degrel.core.{Label, Vertex, VertexHeader}
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.front.BinOp
import degrel.utils.PrettyPrintOptions

abstract class IntegerPredicate extends Rewriter {
  def label: Label

  def pred(lhs: Int, rhs: Int): Boolean

  override def rewrite(target: VertexHeader, parent: Driver): RewriteResult = {
    if (target.label != label) return RewriteResult.NOP
    val resultOpt = for {
      lhs <- target.thru(Label.E.lhs).headOption
      rhs <- target.thru(Label.E.rhs).headOption
      lhsValue <- lhs.getValue[Int]
      rhsValue <- rhs.getValue[Int]
    } yield pred(lhsValue, rhsValue)

    resultOpt match {
      case Some(result) => {
        target.write(Vertex.fromBoolean(result))
        RewriteResult(done = true)
      }
      case _ => RewriteResult.NOP
    }
  }

  override def pp(implicit opt: PrettyPrintOptions): String = s"<BinOp ${label.expr}>"
}

object IntegerPredicate {
  val all = Seq(new Eq(), new Neq(), new Lt(), new Lte(), new Gt(), new Gte())
}

class Eq extends IntegerPredicate {
  override val label: Label = BinOp.EQUALS.toLabel

  override def pred(lhs: Int, rhs: Int): Boolean = lhs == rhs
}

class Neq extends IntegerPredicate {
  override val label: Label = BinOp.NOT_EQUALS.toLabel

  override def pred(lhs: Int, rhs: Int): Boolean = lhs != rhs
}

class Lt extends IntegerPredicate {
  override val label: Label = BinOp.LESS.toLabel

  override def pred(lhs: Int, rhs: Int): Boolean = lhs < rhs
}

class Lte extends IntegerPredicate {
  override val label: Label = BinOp.LESS_EQUAL.toLabel

  override def pred(lhs: Int, rhs: Int): Boolean = lhs <= rhs
}

class Gt extends IntegerPredicate {
  override val label: Label = BinOp.GREATER.toLabel

  override def pred(lhs: Int, rhs: Int): Boolean = lhs > rhs
}

class Gte extends IntegerPredicate {
  override val label: Label = BinOp.GREATER_EQUAL.toLabel

  override def pred(lhs: Int, rhs: Int): Boolean = lhs >= rhs
}
