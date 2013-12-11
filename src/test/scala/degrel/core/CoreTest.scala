package degrel.core

import org.scalatest.FlatSpec

import degrel.front
import degrel.front.ParserUtils

class CoreTest extends FlatSpec {
  val parser = front.DefaultTermParser

  def parse(s: String): Vertex = ParserUtils.parseVertex(s).freeze

  it should "equals simple vertex" in {
    assert(parse("foo") === parse("foo"))
  }

  it should "equals complex graph" in {
    val v1 = parse("foo(bar: baz(a: b), hoge: fuga)")
    val v2 = parse("foo(bar: baz(a: b), hoge: fuga)")
    assert(v1 === v2)
  }

  it should "equals different order" in {
    val v1 = parse("foo(hoge: fuga, bar: baz(a: b))")
    val v2 = parse("foo(bar: baz(a: b), hoge: fuga)")
    assert(v1 === v2)
  }

  it should "部分に違う頂点を含む場合は一致しない" in {
    val v1 = parse("foo(hoge: fuga, bar: baz(a: b(c: d)))")
    val v2 = parse("foo(bar: baz(a: b(c: x)), hoge: fuga)")
    assert(v1 != v2)
  }
}
