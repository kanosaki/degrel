package degrel.core

import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class CoreTest extends FlatSpec {
  def parse(s: String): Vertex = degrel.parseVertex(s)

  it should "equals simple vertex" in {
    assert(parse("foo") ===~ parse("foo"))
  }

  it should "equals complex graph" in {
    val v1 = parse("foo(bar: baz(a: b), hoge: fuga)")
    val v2 = parse("foo(bar: baz(a: b), hoge: fuga)")
    assert(v1 ===~ v2)
  }

  it should "equals different order" in {
    val v1 = parse("foo(hoge: fuga, bar: baz(a: b))")
    val v2 = parse("foo(bar: baz(a: b), hoge: fuga)")
    assert(v1 ===~ v2)
  }

  it should "部分に違う頂点を含む場合は一致しない" in {
    val v1 = parse("foo(hoge: fuga, bar: baz(a: b(c: d)))")
    val v2 = parse("foo(bar: baz(a: b(c: x)), hoge: fuga)")
    assert(v1 ==/~ v2)
  }
}
