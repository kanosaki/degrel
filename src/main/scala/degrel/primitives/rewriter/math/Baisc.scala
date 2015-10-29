package degrel.primitives.rewriter.math

import degrel.core._
import degrel.primitives.rewriter.ValueBinOp

class IntAdd extends ValueBinOp[Int, Int, Int] {
  override val label = Label("+")

  override def calc(lhs: Int, rhs: Int): Int = lhs + rhs
}

class IntSub extends ValueBinOp[Int, Int, Int] {
  override val label = Label("-")

  override def calc(lhs: Int, rhs: Int): Int = lhs - rhs
}

class IntMul extends ValueBinOp[Int, Int, Int] {
  override val label = Label("*")

  override def calc(lhs: Int, rhs: Int): Int = lhs * rhs
}

class IntDiv extends ValueBinOp[Int, Int, Int] {
  override val label = Label("/")

  override def calc(lhs: Int, rhs: Int): Int = lhs / rhs
}

class IntPow extends ValueBinOp[Int, Int, Int] {
  override val label = Label("**")

  override def calc(lhs: Int, rhs: Int): Int = Math.pow(lhs, rhs).asInstanceOf[Int]
}

class Modular extends ValueBinOp[Int, Int, Int] {
  override val label = Label("%")

  override def calc(lhs: Int, rhs: Int): Int = lhs % rhs
}

class DoubleAdd extends ValueBinOp[Double, Double, Double] {
  override val label = Label("+")

  override def calc(lhs: Double, rhs: Double): Double = lhs + rhs
}

class DoubleSub extends ValueBinOp[Double, Double, Double] {
  override val label = Label("-")

  override def calc(lhs: Double, rhs: Double): Double = lhs - rhs
}

class DoubleMul extends ValueBinOp[Double, Double, Double] {
  override val label = Label("*")

  override def calc(lhs: Double, rhs: Double): Double = lhs * rhs
}

class DoubleDiv extends ValueBinOp[Double, Double, Double] {
  override val label = Label("/")

  override def calc(lhs: Double, rhs: Double): Double = lhs / rhs
}

class DoublePow extends ValueBinOp[Double, Double, Double] {
  override val label = Label("**")

  override def calc(lhs: Double, rhs: Double): Double = Math.pow(lhs, rhs)
}
