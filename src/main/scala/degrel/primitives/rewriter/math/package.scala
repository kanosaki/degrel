package degrel.primitives.rewriter

package object math {

  val default = Seq(
    new IntAdd(), new IntSub(), new IntMul(), new IntDiv(), new IntPow(), new Modular(),
    new DoubleAdd(), new DoubleSub(), new DoubleMul(), new DoubleDiv(), new IntPow())
}
