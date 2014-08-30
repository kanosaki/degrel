package degrel.front

import org.scalatest.FlatSpec

object ParseTestUtils {
  def mkVertex(lbl: String, edges: Iterable[(String, AstVertex)] = Map(), attributes: Map[String, String] = Map()) = {
    val attrs = if (attributes.isEmpty) {
      None
    } else {
      Some(attributes.map { case (k, v) => AstAttribute(k, v)}.toSeq)
    }
    AstVertex(AstName(None, Some(AstLabel(lbl))), attrs, mkEdges(edges))
  }

  def mkEdges(edges: Iterable[(String, AstVertex)]): Seq[AstEdge] = {
    edges.map {
      case (l, v) => AstEdge(AstLabel(l), v)
    }.toSeq
  }

  def mkName(label: String = null, capture: String = null) = {
    AstName(wrapIfNotNull(capture, AstCapture), wrapIfNotNull(label, AstLabel))
  }

  def wrapIfNotNull[X, Y](v: X, wrapper: X => Y): Option[Y] = {
    if (v == null)
      None
    else
      Some(wrapper(v))
  }
}

class DefaultTermParserTest extends FlatSpec {

  import degrel.front.ParseTestUtils._

  val parser = DefaultTermParser

  def assert_graph(expr: String, expected: AstRoot) {
    assert(AstGraph(Seq(expected)) === DefaultTermParser(expr).root)
  }

  it should "parse a graph of a vertex" in {
    assert_graph("foo", mkVertex("foo", Seq()))
  }

  it should "parse term simple labeled graph" in {
    assert_graph("foo(bar:baz, joe:jack(duke:togo))",
      mkVertex("foo",
        Seq(("bar", mkVertex("baz")),
          ("joe", mkVertex("jack",
            Seq(("duke", mkVertex("togo")))))
        ))
    )
  }

  it should "parse term with capture" in {
    assert_graph("Hoge[snake](eater: X[foo])",
      AstVertex(mkName(label = "snake", capture = "Hoge"),
        None,
        mkEdges(Seq(("eater",
          AstVertex(mkName(label = "foo", capture = "X"),
            None,
            Seq())))))
    )
  }

  it should "parse rules" in {
    assert_graph("foo(bar: baz) -> hoge(fuga: piyo)",
      AstRule(mkVertex("foo", Seq(("bar", mkVertex("baz")))),
        mkVertex("hoge", Seq(("fuga", mkVertex("piyo"))))
      )
    )
  }

  it should "parse rules with capturing" in {
    assert_graph("foo(bar: X[baz]) -> hoge(fuga: X)",
      AstRule(mkVertex("foo", Seq(("bar", AstVertex(mkName(label = "baz", capture = "X"),
        None,
        Seq())))),
        mkVertex("hoge", Seq(("fuga",
          AstVertex(mkName(capture = "X"),
            None,
            Seq()))))
      )
    )
  }

  it should "reject unmatched braces" in {
    intercept[SyntaxError] {
      parser("foo(bar: X[baz] -> hoge(fuga: X)")
    }
  }

  it should "parse attribute" in {
    assert_graph("foo{hoge: fuga, a: b}(bar: baz)",
      mkVertex("foo", Seq("bar" -> mkVertex("baz")), Map("hoge" -> "fuga", "a" -> "b")))
  }
}
