package degrel.utils.collection

import org.scalatest.FlatSpec

class WeakMultiMapTest extends FlatSpec {
  it should "behave as a normal MultiMap" in {
    val map = new WeakMultiMap[String, Object]()
    val values = Seq("x" -> "foo",
                     "x" -> "bar",
                     "x" -> "baz",
                     "y" -> "hoge",
                     "y" -> "fuga")
    map.addBindings(values)
    assert(map.get("x").get === Set("foo", "bar", "baz"))
    assert(map.get("y").get === Set("hoge", "fuga"))
    assert(map.entryExists("x", _ == "foo"))
    assert(map.entryExists("x", _ == "bar"))
    assert(map.entryExists("x", _ == "baz"))
    assert(!map.entryExists("y", _ == "baz"))
    assert(map.entryExists("y", _ == "hoge"))
    map.removeBinding("x", "foo")
    assert(map.get("x").get === Set("bar", "baz"))
    assert(map.get("y").get === Set("hoge", "fuga"))
    assert(!map.entryExists("x", _ == "foo"))
    assert(map.entryExists("x", _ == "bar"))
    assert(map.entryExists("x", _ == "baz"))
    map.removeBinding("x", "bar")
    assert(map.get("x").get === Set("baz"))
    assert(map.get("y").get === Set("hoge", "fuga"))
    assert(!map.entryExists("x", _ == "foo"))
    assert(!map.entryExists("x", _ == "bar"))
    assert(map.entryExists("x", _ == "baz"))
  }

}
