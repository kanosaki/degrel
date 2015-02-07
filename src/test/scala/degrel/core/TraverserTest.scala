package degrel.core

import degrel.front.{ParserUtils, TermParser}
import org.scalatest.FlatSpec

class TraverserTest extends FlatSpec {
  val parse = degrel.parseVertex _

  it should "traverse none if depth is 0" in {
    val graph = parse("foo(bar: baz, hoge: fuga(a: b))")
    val traversed = Traverser(graph, 0).toSeq
    assert(traversed.size === 1)
    assert(traversed.map(_.label.expr).toSet === Set("foo"))
  }

  it should "traverse with depth" in {
    val graph = parse("foo(bar: baz(x: y, p: q), hoge: fuga(a: b))")
    val traversed = Traverser(graph, 1).toSeq
    assert(traversed.size === 3)
    assert(traversed.map(_.label.expr).toSet === Set("foo", "baz", "fuga"))
  }

  it should "traverse vertices" in {
    val graph = parse("foo(bar: baz, hoge: fuga(a: b))")
    val traversed = Traverser(graph).toSeq
    assert(traversed.size === 4)
    assert(traversed.map(_.label.expr).toSet === Set("foo", "baz", "fuga", "b"))
  }

  it should "work with edge predicate" in {
    val graph = parse("foo(bar: baz, hoge: fuga, piyo: hoge)")
    val traversed = Traverser(
      graph,
      edgePred =
        e => e.label != Label("hoge") && e.dst.label != Label("hoge")).
      toSeq
    assert(traversed.map(_.label).toSet === Set("foo", "baz"))
  }

  it should "work with edge predicate and hop limit" in {
    val graph = parse("foo(bar: baz(x: y, p: q(r: s)), hoge: fuga(a: b))")
    val traversed = Traverser(
      graph,
      2,
      edgePred =
        e => e.label != Label("hoge") && e.label != Label("x")).
      toSeq
    assert(traversed.map(_.label).toSet === Set("foo", "baz", "q"))
  }
}
