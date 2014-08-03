package degrel.utils.collection.mutable

import org.scalatest.FlatSpec

class BiMapTest extends FlatSpec {
  type BiMapImpl[K, V] = BiHashMap[K, V]

  it should "behave as usual mutable.Map for existing entries" in {
    val map = new BiMapImpl[String, String]()
    val pairs = Seq("foo" -> "bar",
                    "hoge" -> "fuga")
    pairs.foreach(kv => map += kv)
    pairs.foreach(
    {
      case (k, v) => {
        assert(Some(v) === map.get(k))
        assert(Some(v) === map.fromKey(k))
        assert(map.get(k) === map.fromKey(k))
      }
    })
  }

  it should "behave as usual mutable.Map on non-exist keys" in {
    val map = new BiMapImpl[String, String]()
    val pairs = Seq("foo" -> "bar",
                    "hoge" -> "fuga")
    pairs.foreach(kv => map += kv)
    assert(map.get("foobar") === None)
    assert(map.fromKey("foobar") === None)
    intercept[NoSuchElementException](
    {
      map("foobar")
    })
  }

  it should "lookup reverse" in {
    val map = new BiMapImpl[String, String]()
    val pairs = Seq("foo" -> "bar",
                    "hoge" -> "fuga")
    pairs.foreach(kv => map += kv)
    pairs.foreach(
    {
      case (k, v) => {
        assert(Some(k) === map.fromValue(v))
      }
    })
  }

  it should "lookup reverse with non-exist values" in {
    val map = new BiMapImpl[String, String]()
    val pairs = Seq("foo" -> "bar",
                    "hoge" -> "fuga")
    pairs.foreach(kv => map += kv)
    assert(map.fromValue("foobar") === None)
  }

  it should "be able to remove elements" in {
    val map = new BiMapImpl[String, String]()
    val pairs = Seq("foo" -> "bar",
                    "hoge" -> "fuga")
    pairs.foreach(kv => map += kv)
    map -= "foo"
    assert(map.get("foo") === None)
    assert(map.fromKey("foo") === None)
    assert(map.fromValue("bar") === None)
    intercept[NoSuchElementException](
    {
      map("foo")
    })
  }
}
