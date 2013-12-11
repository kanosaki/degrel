package degrel.engine

import org.scalatest.FlatSpec
import degrel.front
import degrel.core
import degrel.front.ParserUtils
import degrel.utils.TestUtils._

class RewriterTest extends FlatSpec {
  val parser = front.DefaultTermParser

  def parse(s: String): core.Vertex = ParserUtils.parseVertex(s)

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
}
