package degrel.utils.collection

import org.scalatest.FlatSpec

class LoopIteratorTest extends FlatSpec {
  it should "empty if source is empty" in {
    val src = Seq()
    val it = LoopIterator(src)
    assert(it.isEmpty)
  }

  it should "loop an sequence" in {
    val src = Seq(1, 2, 3)
    val it = LoopIterator(src)
    assert(it.take(9).toSeq === (src ++ src ++ src))
    assert(it.take(12).toSeq === (src ++ src ++ src ++ src))
  }
}
