package degrel.primitives.rewriter.lang

import degrel.core.{Label, Vertex, VertexHeader}
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.front.BinOp
import degrel.primitives.rewriter.ValueBinOp
import degrel.utils.PrettyPrintOptions

object IntegerPredicate {
  val all = Seq(new Eq(), new Neq(), new Lt(), new Lte(), new Gt(), new Gte())

  class Eq extends ValueBinOp[Int, Int, Boolean] {
    override val label: Label = BinOp.EQUALS.toLabel

    override def calc(lhs: Int, rhs: Int): Boolean = lhs == rhs
  }

  class Neq extends ValueBinOp[Int, Int, Boolean] {
    override val label: Label = BinOp.NOT_EQUALS.toLabel

    override def calc(lhs: Int, rhs: Int): Boolean = lhs != rhs
  }

  class Lt extends ValueBinOp[Int, Int, Boolean] {
    override val label: Label = BinOp.LESS.toLabel

    override def calc(lhs: Int, rhs: Int): Boolean = lhs < rhs
  }

  class Lte extends ValueBinOp[Int, Int, Boolean] {
    override val label: Label = BinOp.LESS_EQUAL.toLabel

    override def calc(lhs: Int, rhs: Int): Boolean = lhs <= rhs
  }

  class Gt extends ValueBinOp[Int, Int, Boolean] {
    override val label: Label = BinOp.GREATER.toLabel

    override def calc(lhs: Int, rhs: Int): Boolean = lhs > rhs
  }

  class Gte extends ValueBinOp[Int, Int, Boolean] {
    override val label: Label = BinOp.GREATER_EQUAL.toLabel

    override def calc(lhs: Int, rhs: Int): Boolean = lhs >= rhs
  }
}

