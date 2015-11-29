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

  it should "traverse vertices with cycled graph" in {
    val graph = parse("foo(bar: baz@B, hoge: fuga(a: B, c: b))")
    val traversed = Traverser(graph).toSeq
    assert(traversed.map(_.label.expr).toSet === Set("foo", "baz", "fuga", "b"))
  }

  it should "traverse with region inner only" in {
    val graph = parse("foo(a, b, c, {d; e; f})")
    val traversed = Traverser(graph, _.label == Label.V.cell, TraverseRegion.InnerOnly).toSeq
    assert(traversed.map(_.label.expr).toSet === Set("foo", "a", "b", "c"))
  }

  it should "traverse with region inner and wall" in {
    val graph = parse("foo(a, b, c, {d; e; f})")
    val traversed = Traverser(graph, _.label == Label.V.cell, TraverseRegion.InnerAndWall).toSeq
    assert(traversed.map(_.label.expr).toSet === Set("foo", "a", "b", "c", "__cell__"))
  }

  it should "return nothing if root is wall and region is inner only." in {
    val graph = parse("foo(a, b, c, {d; e; f})")
    val traversed = Traverser(graph, _.label == Label("foo"), TraverseRegion.InnerOnly).toSeq
    assert(traversed.map(_.label.expr).toSet === Set())
  }

  it should "return root if root is wall and region is inner andn wall." in {
    val graph = parse("foo(a, b, c, {d; e; f})")
    val traversed = Traverser(graph, _.label == Label("foo"), TraverseRegion.InnerAndWall).toSeq
    assert(traversed.map(_.label.expr).toSet === Set("foo"))
  }

  it should "traverse all wall vertices and its inner vertices" in {
    val graph = parse("bar(foo1(foo2, foo3, c, {d; e; f}), baz)")
    val traversed = Traverser(graph, _.label.expr.startsWith("foo"), TraverseRegion.InnerAndWall).toSeq
    assert(traversed.map(_.label.expr).toSet === Set("bar", "baz", "foo1", "foo2", "foo3"))
  }

  it should "traverse all wall vertices" in {
    val graph = parse("bar(foo1(foo2, foo3, c, {d; e; f}), baz)")
    val traversed = Traverser(graph, _.label.expr.startsWith("foo"), TraverseRegion.WallOnly).toSeq
    assert(traversed.map(_.label.expr).toSet === Set("foo1", "foo2", "foo3"))
  }

  it should "traverse all cell contents" in {
    val graph = parse("{foo; bar(hoge, {fuga}); baz {hoge; fuga}}")
    val traversed = Traverser(graph, TraverserCutOff.cell(graph)).toSeq
    assert(traversed.map(_.label.expr).toSet === Set("__cell__", "foo", "bar", "baz", "hoge"))
  }
}
