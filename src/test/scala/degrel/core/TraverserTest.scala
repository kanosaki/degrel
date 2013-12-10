package degrel.core

import org.scalatest.FlatSpec
import degrel.front.{DefaultTermParser, ParserUtils}

class TraverserTest extends FlatSpec {
  val parser = DefaultTermParser

  def parse(s: String): Vertex = ParserUtils.parseVertex(s)

  it should "traverse vertices" in {
    val graph = parse("foo(bar: baz, hoge: fuga(a: b))")
    val traversed = Traverser(graph).toSeq
    assert(traversed.size === 4)
  }
  // TODO: Write more
}
