package degrel.utils

import org.scalatest.FlatSpec

class TreeMapTest extends FlatSpec {

  it should "store mapping in single node" in {
    val map = TreeMap.empty[String, String]()
    map.bindSymbol("foo", "bar")
    map.bindSymbol("hoge", "fuga")
    map.bindSymbol("hoge", "piyo")

    assert(map.resolve("foo") === List("bar"))

    assert(map.resolveExact("foo") === "bar")
    assert(map.resolveExact("foobar", Some("default")) === "default")

    assert(map.resolveGrouped("foo") === List(List("bar")))

    intercept[NameError] {
      map.resolveExact("hoge")
    }
  }

  it should "store and resolve mapping in tree" in {
    val root = TreeMap.empty[String, String]()
    val c1 = TreeMap.child(root)
    root.bindSymbol("foo", "bar")
    root.bindSymbol("c", "d")
    root.bindSymbol("hoge", "fuga")
    root.bindSymbol("hoge", "piyo")

    c1.bindSymbol("foo", "baz")
    c1.bindSymbol("a", "b")


    assert(c1.resolveExact("a") === "b")
    assert(c1.resolveExact("c") === "d")
    assert(c1.resolve("a") === List("b"))
    assert(c1.resolve("c") === List("d"))
    assert(c1.resolve("foo") === List("baz", "bar"))

    assert(c1.resolveGrouped("foo") === List(List("baz"), List("bar")))
    assert(c1.resolveGrouped("hoge") === List(List(), List("piyo", "fuga")))
  }

  it should "Lookup data in various methods" in {
    val map = TreeMap.empty[String, String]()
    map.bindSymbol("foo", "bar")
    map.bindSymbol("hoge", "fuga")
    map.bindSymbol("hoge", "piyo")
  }
}
