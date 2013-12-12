package degrel.rewriting

import org.scalatest.FlatSpec
import org.scalatest.time.SpanSugar._
import org.scalatest.concurrent.Timeouts._
import degrel.front
import degrel.core
import degrel.front.ParserUtils
import degrel.utils.TestUtils._

class RewriterTest extends FlatSpec {
  val parser = front.DefaultTermParser

  def parse(s: String): core.Vertex = ParserUtils.parseVertex(s)

  def parseR(s: String): core.Rule = ParserUtils.parseRule(s)

  it should "build simple vertex" in {
    val dataV = parse("foo(bar: baz)")
    val rule = parse("foo(bar: A) -> foo(hoge: A)").asRule
    val binding = dataV.matches(rule.lhs).pack.pickFirst
    val result = rule.rhs.build(new BuildingContext(binding))
    assert(result ===~ parse("foo(hoge: baz)"))
  }

  it should "build with multiple vertex" in {
    val dataV = parse("foo(bar: baz, x: a(b: c(d: e)))")
    val rule = parse("foo(bar: A, x: a(b: B)) -> foo(hoge: A, fuga: B)").asRule
    val mch = dataV.matches(rule.lhs)
    val packed = mch.pack
    val binding = packed.pickFirst
    val result = rule.rhs.build(new BuildingContext(binding))
    assert(result ===~ parse("foo(hoge: baz, fuga: c(d: e))"))
  }

  it should "rewrite single vertex with single rule" in {
    val reserve = new LocalReserve()
    reserve.addRule(parseR("a -> b"))
    reserve.addVertex(parse("a"))
    val rewrote = reserve.rewriteStep()
    assert(rewrote, "should be written")
    val expected = Set(parse("b")).map(_.freeze)
    val actual = reserve.freeze.roots.toSet
    assert(expected === actual)
  }

  it should "rewrite single vertex with single rule at deep level" in {
    val reserve = new LocalReserve()
    reserve.addRule(parseR("a -> b"))
    reserve.addVertex(parse("x(y: z(c: a))"))
    val rewrote = reserve.rewriteStep()
    assert(rewrote, "should be written")
    val expected = Set(parse("x(y: z(c: b))")).map(_.freeze)
    val actual = reserve.freeze.roots.toSet
    assert(expected === actual)
  }

  it should "rewrite single vertex if there are no rules which have nothing to do with" in {
    val reserve = new LocalReserve()
    reserve.addRule(parseR("a -> b(c: d)"))
    reserve.addRule(parseR("z -> y"))
    reserve.addVertex(parse("x(y: z(c: a))"))
    reserve.addVertex(parse("hoge(fuga: piyo)"))
    val rewrote = reserve.rewriteStep()
    assert(rewrote, "should be written")
    val expected = Set(parse("x(y: z(c: b(c: d)))"),
                        parse("hoge(fuga: piyo)")).map(_.freeze)
    val actual = reserve.freeze.roots.toSet
    assert(expected === actual)
  }

  it should "rewrite single vertex with capturing" in {
    val reserve = new LocalReserve()
    reserve.addRule(parseR("a(b: X) -> b(c: X)"))
    reserve.addVertex(parse("x(y: z(c: a(b: foo(bar: baz))))"))
    val rewrote = reserve.rewriteStep()
    assert(rewrote, "should be written")
    val expected = Set(parse("x(y: z(c: b(c: foo(bar: baz))))")).map(_.freeze)
    val actual = reserve.freeze.roots.toSet
    assert(expected === actual)
  }

  it should "rewrite in multi steps" in {
    val reserve = new LocalReserve()
    reserve.addRule(parseR("a(x: X) -> c(d: b(y: X))"))
    reserve.addRule(parseR("b(y: X) -> foo(bar: X)"))
    reserve.addVertex(parse("a(x: foo)"))
    reserve.addVertex(parse("x(y: a(x: hoge(fuga: piyo)), b: c)"))
    failAfter(1 seconds) {
      reserve.rewriteUntilStop()
    }
    val expected = Set(parse("x(y: c(d: foo(bar: hoge(fuga: piyo))), b: c)"),
                        parse("c(d: foo(bar: foo))")).map(_.freeze)
    val actual = reserve.freeze.roots.toSet
    assert(expected === actual)
  }
}
