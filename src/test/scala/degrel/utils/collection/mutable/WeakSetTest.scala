package degrel.utils.collection.mutable

import org.scalatest.FlatSpec

import scala.collection.mutable

class WeakSetTest extends FlatSpec {
  def testSet(s: mutable.Set[String]) = {
    s ++= Seq("a", "b", "c")
    assert(s.contains("a"))
    assert(s.contains("b"))
    assert(s.contains("c"))
    assert(!s.contains("d"))
    s += "d"
    assert(s.contains("a"))
    assert(s.contains("b"))
    assert(s.contains("c"))
    assert(s.contains("d"))
    s -= "b"
    assert(s.contains("a"))
    assert(!s.contains("b"))
    assert(s.contains("c"))
    assert(s.contains("d"))
    s -= "d"
    assert(s.contains("a"))
    assert(!s.contains("b"))
    assert(s.contains("c"))
    assert(!s.contains("d"))
    s -= "a"
    assert(!s.contains("a"))
    assert(!s.contains("b"))
    assert(s.contains("c"))
    assert(!s.contains("d"))
    s -= "c"
    assert(!s.contains("a"))
    assert(!s.contains("b"))
    assert(!s.contains("c"))
    assert(!s.contains("d"))
  }

  "WeakLinkedSet" should "behave as normal Set" in {
    testSet(new WeakLinkedSet[String]())
  }

  "WeakHashSet" should "behave as normal Set" in {
    testSet(new WeakLinkedSet[String]())
  }
}
