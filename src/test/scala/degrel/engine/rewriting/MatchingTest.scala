package degrel.engine.rewriting

import degrel.front.Parser
import degrel.utils.FlyWrite._
import degrel.utils.TestUtils._
import degrel.utils.VertexExtensions._
import degrel.{core, graphbuilder}
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

  it should "lookup vertex by attribute" in {
    val pattern = parse("foo{id: 0}(bar: baz, hoge: _{id: 1})")
    val vertex = parse("foo(bar: baz, hoge: fuga)")
    val mch = vertex.matches(pattern)
    assert(mch.success)
    val pack = mch.pack
    val binding = pack.pickFirst.asQueryable
    val (_, dataV) = binding.query(_.hasId("1")).head
    assert(dataV.label === "fuga")
  }

  it should "bridge vertices" in {
    val pattern = parse("foo(bar: baz{id: 1}, joe: hoge(fuga: _{id: 2}, piyo: _{id: 3}))")
    val vertex = parse("foo(bar: baz, joe: hoge(fuga: a, piyo: b))")
    val mch = vertex.matches(pattern)
    assert(mch.success)
    val pack = mch.pack
    val binding = pack.pickFirst.asQueryable
    val assertData = Seq("1" -> "baz", "2" -> "a", "3" -> "b")
    for ((id, targetLabel) <- assertData) {
      val it = binding.query(_.hasId(id))
      val (_, dataV) = it.head
      assert(dataV.label === targetLabel)
    }
  }

  it should "bridge vertices with more complex graph with many wildcard vertex" in {
    val pattern = parse("foo(bar: _{id: 1}, joe: hoge(fuga: _{id: 2}, piyo: _{id: 3}))")
    val vertex = parse("foo(bar: baz, joe: hoge(fuga: a, piyo: b))")
    val mch = vertex.matches(pattern)
    assert(mch.success)
    val pack = mch.pack
    val binding = pack.pickFirst.asQueryable
    val assertData = Seq("1" -> "baz", "2" -> "a", "3" -> "b")
    for ((id, targetLabel) <- assertData) {
      val it = binding.query(_.hasId(id))
      val (_, dataV) = it.head
      assert(dataV.label === targetLabel)
    }
  }

  it should "bridge vertex and following graph" in {
    val pattern = parse("foo(bar: _{id: 1}, joe: hoge(fuga: _{id: 2}, piyo: _{id: 3}))")
    val vertex = parse("foo(bar: a(a: b), joe: hoge(fuga: a(b: c), piyo: a))")
    val mch = vertex.matches(pattern)
    assert(mch.success)
    val pack = mch.pack
    val binding = pack.pickFirst.asQueryable
    val assertData = Seq("1" -> "a(a: b)", "2" -> "a(b: c)", "3" -> "a")
    for ((id, target) <- assertData) {
      val it = binding.query(_.hasId(id))
      val (_, dataV) = it.head
      assert(dataV ===~ parse(target))
    }
  }
}