package degrel.utils.collection.mutable

import org.scalatest.FlatSpec

class ConcurrentHashSetTest extends FlatSpec {
  it should "behave usual Set" in {
    val set = new ConcurrentHashSet[String]()
    val elems = Seq("FOO", "BAR", "BAZ")
    elems.foreach(set += _)
    elems.foreach(e => {
      assert(set.contains(e))
    })
    assert(!set.contains("HOGE"))
    set += "HOGE"
    assert(set.contains("HOGE"))
  }

  it should "putIfAbsent" in {
    val set = new ConcurrentHashSet[String]()
    val v = "FOOBAR"
    assert(!set.contains(v))
    assert(set.putIfAbsent(v))
    assert(set.contains(v))
    assert(!set.putIfAbsent(v))
  }
}
