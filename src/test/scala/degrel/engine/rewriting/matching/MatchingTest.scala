package degrel.engine.rewriting.matching

import degrel.front.Parser
import degrel.utils.FlyWrite._
import degrel.utils.TestUtils._
import degrel.utils.VertexExtensions._
import degrel.{core, degrel.front.graphbuilder}
import org.scalatest.FlatSpec

/**
 * Around matching and (Mono)binding
 */
class MatchingTest extends FlatSpec {
  val parser = Parser.vertex _

  def parse(s: String): core.Vertex = graphbuilder.build(Parser.vertex(s))

  it should "match single vertex" in {
    val a = vHead("foo").matches(vHead("foo"))
    assert(a.success)
  }

  it should "not matches different labeled vertex" in {
    val pattern = vHead("foo")
    val verticies = Seq(
      vHead("hoge"),
      vHead("bar"),
      "baz" |^|("piyo", vHead("hoge")))
    for (v <- verticies) {
      val mch = v.matches(pattern)
      assert(!mch.success)
    }
  }

  it should "all vertex matches with wildcard vertex" in {
    val pattern = vHead("_")
    val verticies = Seq(
      vHead("foo"),
      vHead("bar"),
      "baz" |^|("foo", vHead("hoge")))
    for (v <- verticies) {
      val mch = v.matches(pattern)
      assert(mch.success)
    }
  }

  it should "matches by partial pattern" in {
    val pattern = parse("foo(bar: _)")
    val verticies = Seq(
      "foo(bar: baz)",
      "foo(hoge: fuga, bar: baz)").map(parse)
    for (v <- verticies) {
      val mch = v.matches(pattern)
      assert(mch.success)
    }
  }

  it should "reject composite pattern with difference edge name" in {
    val pattern = parse("foo(piyo: _)")
    val verticies = Seq(
      "foo(bar: baz)",
      "foo(hoge: fuga, bar: baz)").map(parse)
    for (v <- verticies) {
      val mch = v.matches(pattern)
      assert(!mch.success)
    }
  }

  it should "reject composite pattern with difference child vertex" in {
    val pattern = parse("foo(piyo: baz(hoge: fuga), bar: _)")
    val verticies = Seq(
      "foo(bar: baz, piyo: bazbaz(hoge: fuga))",
      "foo(hoge: fuga, bar: baz)").map(parse)
    for (v <- verticies) {
      val mch = v.matches(pattern)
      assert(!mch.success)
    }
  }

  it should "bridge a vertex" in {
    val pattern = parse("foo(bar: baz, hoge: _)")
    val vertex = parse("foo(bar: baz, hoge: fuga)")
    val mch = vertex.matches(pattern)
    assert(mch.success)
    val pack = mch.pack
    val binding = pack.pickFirst.asQueryable
    val (patV, dataV) = binding.query(_.label == "_").head
    assert(patV.label === "_")
    assert(dataV.label === "fuga")
  }
}
