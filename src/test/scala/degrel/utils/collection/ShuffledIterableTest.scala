package degrel.utils.collection

import org.scalatest.FlatSpec

class ShuffledIterableTest extends FlatSpec {

  it should "iterates empty iterator" in {
    val src = Seq()
    val shuffled = ShuffledIterator(src.iterator)
    assert(!shuffled.hasNext)
    assert(shuffled.toSeq == Seq())
  }

  it should "shuffle iterator" in {
    val src = (1 to 100).toSeq
    val shuffled = ShuffledIterator(src.iterator, 10)
    val result = shuffled.toSeq
    assert(src !== result) // In almost case, these will be different seq.
    assert(src.toSet === result.toSet)
  }
}
