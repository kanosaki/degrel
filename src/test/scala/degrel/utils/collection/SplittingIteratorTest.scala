package degrel.utils.collection

import org.scalatest.FlatSpec

class SplittingIteratorTest extends FlatSpec {
  it should "return empty seq if source is empty" in {
    val src = Seq()
    val expected = Seq()
    val actual = split(src)
    assert(expected === actual)
  }

  it should "usual case" in {
    val src = Seq(0, 2, 2, 3, 1, 1, 1, 4)
    val expected = Seq(Seq(0), Seq(2, 2), Seq(3), Seq(1, 1, 1), Seq(4))
    val actual = split(src)
    assert(actual === expected)
  }
}
