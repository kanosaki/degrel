package degrel.front.dotlike

import org.scalatest.FlatSpec
import degrel.core.{Vertex, BasicLabel}
import degrel.Query._

class DotlikeTest extends FlatSpec {

  class RawIdentifier(expr: String) extends AstDigraphIdentifier(expr match { case "" => None; case s => Some(s)},
                                                                 None) {
    override def toIdentifier: String = expr
  }

  def mkEdge(from: String, to: String, label: String): AstDigraphEdge = {
    AstDigraphEdge(new RawIdentifier(from), new RawIdentifier(to), label)
  }

  it should "parse root only digraph" in {
    val expr = "@an_root{}"
    val expected = AstDigraph("an_root", AstDigraphBody(Seq()))
    val actual = DigraphParser(expr)
    assert(expected === actual)
  }

  it should "parse digraph which explains a tree with semicolon separeted" in {
    val expr =
      """@root { -> a : e;
        |  -> b : e;
        |  a -> aa : e;
        |  a -> ab : e;
        |  b -> ba : e;
        |  b -> bb : e;
        |}
      """.stripMargin
    val expectedAstEdges = Set(mkEdge("", "a", "e"),
                               mkEdge("", "b", "e"),
                               mkEdge("a", "aa", "e"),
                               mkEdge("a", "ab", "e"),
                               mkEdge("b", "ba", "e"),
                               mkEdge("b", "bb", "e"))
    val actualAst = DigraphParser(expr)
    assert(actualAst.label === "root")
    assert(actualAst.body.elements.toSet === expectedAstEdges)
  }

  it should "parse digraph which explains a tree sepeareted by newline" in {
    val expr =
      """@root {
        |  -> a : e
        |  -> b : e
        |  a -> aa : e
        |  a -> ab : e
        |  b -> ba : e
        |  b -> bb : e
        |  a {foo: bar}
        |  b {hoge: fuga, foo: bar}
        |}
      """.stripMargin
    val expectedAstEdges = Set(mkEdge("", "a", "e"),
                               mkEdge("", "b", "e"),
                               mkEdge("a", "aa", "e"),
                               mkEdge("a", "ab", "e"),
                               mkEdge("b", "ba", "e"),
                               mkEdge("b", "bb", "e"),
                               AstDigraphAttributes("a", Seq("foo" -> "bar")),
                               AstDigraphAttributes("b", Seq("hoge" -> "fuga", "foo" -> "bar")))
    val actualAst = DigraphParser(expr)
    assert(actualAst.label === "root")
    assert(actualAst.body.elements.toSet === expectedAstEdges)
  }

  it should "build simple vertex" in {
    val expr = "@root{}"
    val ast = DigraphParser(expr)
    val graph = ast.toGraph()
    assert(graph.label === BasicLabel("root"))
    assert(graph.edges().size === 0)
    assert(graph.attributes === Map())
  }

  it should "building vertex 1" in {
    val expr =
      """@root{
        |  -> a : e
        |  a -> b : e
        |  b -> c : e
        |  c -> a : e
        |}""".stripMargin
    val ast = DigraphParser(expr)
    val graph = ast.toGraph()
    val a = graph.path("a").exact.asInstanceOf[Vertex]
    assert(a.edges().size === 1)
    val b = graph.path("a/b").exact.asInstanceOf[Vertex]
    assert(b.edges().size === 1)
    val c = graph.path("a/b/c").exact.asInstanceOf[Vertex]
    assert(c.edges().size === 1)
    assert(graph.path("a/b/c/a").exact === graph.path("a").exact)
    // (root -> a -> b -> c ->) a
    assert(graph.path(":e/:e/:e/:e").nextV().exact === graph.path("a").exact)
  }

  it should "building vertex 2" in {
    val expr =
      """@root{
        |  -> a : e
        |  a -> b : e
        |  a -> c : e
        |  b -> c : e
        |  c -> d : e
        |  b -> d : e
        |  d -> c : e
        |  d -> b : e
        |}""".stripMargin
    val ast = DigraphParser(expr)
    val graph = ast.toGraph()
    val a = graph.path("a").exact.asInstanceOf[Vertex]
    assert(a.edges().size === 2)
    val b = graph.path("a/b").exact.asInstanceOf[Vertex]
    assert(b.edges().size === 2)
    val c = graph.path("a/c").exact.asInstanceOf[Vertex]
    assert(c.edges().size === 1)
  }

  it should "build graph with identifier" in {
    val expr =
      """@root{
        |   -> a : e
        |   a -> b$1 : e
        |   a -> b$2 : e
        |   a -> b$3 : e
        |}
      """.stripMargin
    val ast = DigraphParser(expr)
    val graph = ast.toGraph()
    val bs = graph.path("a/b").toSeq
    assert(bs.size === 3)
  }

  it should "build a DAG with identifier" in {
    val expr =
      """@root{
        |  -> a$1 : e
        |  a$1 -> a$2 : e
        |  a$2 -> a$3 : e
        |  a$3 -> a$1 : e
        |
        |  a$3 -> b : foo
        |  a$1 -> c : bar
        |}
      """.stripMargin
    val ast = DigraphParser(expr)
    val graph = ast.toGraph()
    val a1q = graph.path("a").toSeq
    assert(a1q.size === 1)
    val a1 = graph.path("a").exactAs[Vertex]
    val a1_2 = a1.path("a/a/a").exactAs[Vertex]
    assert(a1 === a1_2)


  }

}
