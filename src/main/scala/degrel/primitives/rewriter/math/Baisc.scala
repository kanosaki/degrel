package degrel.primitives.rewriter.math

import degrel.core._
import degrel.primitives.rewriter.ValueBinOp

class Plus extends ValueBinOp[Int, Int, Int] {
  override val label = Label("+")

  override def calc(lhs: Int, rhs: Int): Int = lhs + rhs
}

class Modular extends ValueBinOp[Int, Int, Int] {
  override val label = Label("%")

  override def calc(lhs: Int, rhs: Int): Int = lhs % rhs
}
