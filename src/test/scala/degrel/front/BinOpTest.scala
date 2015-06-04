package degrel.front

import org.scalatest.FlatSpec

class BinOpTest extends FlatSpec {
  it should "sort by precedence" in {
    val ops = Seq(
      BinOp.ADD, // +
      BinOp.BIND, // ->
      BinOp.MUL // *
    )
    assert(ops.sorted === Seq(BinOp.MUL, BinOp.ADD, BinOp.BIND))
  }

  it should "sort by precedence with same precedence operators" in {
    val op1 = BinOp("+", 0, OpAssoc.Left)
    val op2 = BinOp("-", 0, OpAssoc.Right)
    val op3 = BinOp("*", 1, OpAssoc.Right)
    val op4 = BinOp("/", 1, OpAssoc.Left)
    val ops = Seq(op1, op2, op3, op4)
    assert(ops.sorted === Seq(op4, op3, op1, op2))
  }
}
