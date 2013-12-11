package degrel.core

import org.scalatest.FlatSpec
import degrel.front.{DefaultTermParser, ParserUtils}

class TraverserTest extends FlatSpec {
  val parser = DefaultTermParser

  def parse(s: String): Vertex = ParserUtils.parseVertex(s)

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
  // TODO: Write more
}
