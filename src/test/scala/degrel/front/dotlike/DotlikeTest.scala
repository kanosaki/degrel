package degrel.front.dotlike

import org.scalatest.FlatSpec

class DotlikeTest extends FlatSpec {
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
    val expectedAstEdges = Set(AstDigraphEdge("", "a", "e"),
                                AstDigraphEdge("", "b", "e"),
                                AstDigraphEdge("a", "aa", "e"),
                                AstDigraphEdge("a", "ab", "e"),
                                AstDigraphEdge("b", "ba", "e"),
                                AstDigraphEdge("b", "bb", "e"))
    val actualAst = DigraphParser(expr)
    assert(actualAst.label === "root")
    assert(actualAst.body.elements.toSet === expectedAstEdges)
  }

  it should "parse digraph which explains a tree sepeareted by newline" in {
    val expr =
      """@root { -> a : e
        |  -> b : e
        |  a -> aa : e
        |  a -> ab : e
        |  b -> ba : e
        |  b -> bb : e
        |  a {foo: bar}
        |  b {hoge: fuga, foo: bar}
        |}
      """.stripMargin
    val expectedAstEdges = Set(AstDigraphEdge("", "a", "e"),
                                AstDigraphEdge("", "b", "e"),
                                AstDigraphEdge("a", "aa", "e"),
                                AstDigraphEdge("a", "ab", "e"),
                                AstDigraphEdge("b", "ba", "e"),
                                AstDigraphEdge("b", "bb", "e"),
                                AstDigraphAttributes("a", Seq("foo" -> "bar")),
                                AstDigraphAttributes("b", Seq("hoge" -> "fuga", "foo" -> "bar")))
    val actualAst = DigraphParser(expr)
    assert(actualAst.label === "root")
    assert(actualAst.body.elements.toSet === expectedAstEdges)
  }

  it should "build simple vertex" in {
    val expr = "@root{}"
  }
}
