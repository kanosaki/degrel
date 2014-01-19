package degrel.core.builders

import org.scalatest.FlatSpec
import degrel.front.dotlike.DigraphParser
import degrel.core.{VertexBody, Vertex}
import degrel.front.ParserUtils
import degrel.Query._

class FreezerTest extends FlatSpec {
  val parser = DigraphParser

  def parseTerm(s: String): Vertex = ParserUtils.parseVertex(s)

  def parseDot(expr: String): Vertex = {
    val ast = parser(expr)
    ast.toGraph()
  }

  it should "freeze treed graph" in {
    val graph = parseTerm("foo(a: bar, b: baz(hoge: fuga))")
    val frozen = graph.freeze
    assert(frozen.path(":b/baz").exact === parseTerm("baz(hoge: fuga)").freeze)
  }

  it should "freeze looped graph" in {
    val graph = parseDot(
                          """@root{
                            |  -> a : e
                            |  a -> b : e
                            |  b -> c : e
                            |  c -> a : e
                            |}
                          """.stripMargin)
    val frozen = graph.freeze
    assert(frozen.path("a").exact.isInstanceOf[VertexBody])
    assert(frozen.path("a/b").exact.isInstanceOf[VertexBody])
    assert(frozen.path("a/b/c").exact.isInstanceOf[VertexBody])
    assert(frozen.path("a/b/c/a").exact.isInstanceOf[VertexBody])
    assert(frozen.path("a/b/c/a").exact eq frozen.path("a").exact)
  }
}
