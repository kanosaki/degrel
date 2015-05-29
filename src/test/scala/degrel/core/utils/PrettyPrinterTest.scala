package degrel.core.utils

import degrel.front.Parser
import degrel.utils.PrettyPrintOptions
import org.scalatest.FlatSpec

class PrettyPrinterTest extends FlatSpec {
  val parse = degrel.parseVertex _
  implicit val ppOption = PrettyPrintOptions(showAllId = false, multiLine = false)

  def removeWs(src: String) = src.replaceAll("\\s", "")

  it should "UtilTest: removeWs" in {
    assert(removeWs("a b c") === "abc")
    assert(removeWs("a\tb\t\nc") === "abc")
  }

  it should "Print single vertex" in {
    val v = parse("foo")
    val actual = pp(v)
    val expected = "foo"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print a functor with single edge" in {
    val v = parse("hoge(fuga: baz)")
    val actual = pp(v)
    val expected = "hoge(fuga: baz)"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print a functor with multiple edge" in {
    val v = parse("hoge(fuga: baz, x: y, piyo: foobar)")
    val actual = pp(v)
    val expected = "hoge(fuga: baz, x: y, piyo: foobar)"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print a functor which is Tree" in {
    val v = parse("hoge(fuga: baz, piyo: foo(bar: baz))")
    val actual = pp(v)
    val expected = "hoge(fuga: baz, piyo: foo(bar: baz))"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print a functor which is Tree, different order edge" in {
    val v = parse("hoge(fuga: x(a: b, y: z, p: c(d: e)), piyo: foo(bar: baz))")
    val actual = pp(v)
    val expected = "hoge(fuga: x(y: z, a: b,p: c(d: e)), piyo: foo(bar: baz))"
    assert(removeWs(actual) !== removeWs(expected))
  }

  it should "Print a rule" in {
    val v = parse("a -> b")
    val actual = pp(v)
    val expected = "a -> b"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print a curried rule" in {
    val v = parse("a -> b -> c")
    val actual = pp(v)
    val expected = "a -> b -> c"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print a rule with functor" in {
    val v = parse("a -> b(x: y) -> c(d: e)")
    val actual = pp(v)
    val expected = "a -> b(x: y) -> c(d: e)"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print an empty cell" in {
    val v = parse("{}")
    val actual = pp(v)
    val expected = "{}"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print an simple cell" in {
    val v = parse("{a}")
    val actual = pp(v)
    val expected = "{a}"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print a cell with rule and root" in {
    val v = parse("{x ->y; a(b: c)}")
    val actual = pp(v)
    val expected = "{a(b: c); x -> y}"
    assert(removeWs(actual) === removeWs(expected))
  }

  it should "Print a functor with reference" in {
    val v = parse("a(b: c@X, d: X)")
    val actual = pp(v)
    val expected = "a\\(b:c@X\\[\\w+\\],d:<c@X\\[\\w+\\]>\\)"
    assert(removeWs(actual).matches(expected), s"${removeWs(actual)} not matches $expected")
  }
}
