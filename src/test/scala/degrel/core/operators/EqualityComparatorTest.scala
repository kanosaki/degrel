package degrel.core.operators

import degrel.core.Graph
import degrel.front.ParserUtils
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class EqualityComparatorTest extends FlatSpec {
  def parse(s: String): Graph = degrel.parseVertex(s).toGraph

  def parseDot(s: String): Graph = ParserUtils.parseDot(s).toGraph
  it should "compare isomorphic simple vertex" in {
    val v1 = parse("foo")
    val v2 = parse("foo")
    assert(v1 ===~ v2)
  }

  it should "compare non-isomorphic simple vertex" in {
    val v1 = parse("foo")
    val v2 = parse("bar")
    assert(v1 ==/~ v2)
  }

  it should "compare tree isomorphic graph without polyvalent edge" in {
    val cases = Seq("foo(bar: hoge(fuga: piyo), baz: abc)" -> "foo(baz: abc, bar: hoge(fuga: piyo))")
    for ((v1, v2) <- cases) {
      parse(v1) ===~ parse(v2)
    }
  }

  it should "compare tree non-isomorphic graph without polyvalent edge" in {
    val cases =
      Seq(
        "foo(bar: hoge(fuga: piyo), baz: abc, joe: jack)"
          -> "foo(baz: abc, bar: hoge(fuga: piyo))",
        "foo(bar: hoge(fuga: piyo), baz: abc)"
          -> "foo(baz: abc, bar: hoge(fuga: piyo), joe: jack)",
        "foo(bar: hoge(fuga: piyo, a: b), baz: abc)"
          -> "foo(baz: abc, bar: hoge(fuga: piyo))",
        "foo(bar: hoge(fuga: piyo), baz: abc)"
          -> "foo(baz: abc, bar: hoge(fuga: piyo, a: b))")
    for ((v1, v2) <- cases) {
      parse(v1) ==/~ parse(v2)
    }
  }

  it should "compare isomorphic graph without polyvalent edge" in {
    val cases = Seq( """@a{ -> b : b; b -> c: c; c -> : a}""" -> """@a{ -> b : b; b -> c: c; c -> : a}""")
    for ((v1, v2) <- cases) {
      parseDot(v1) ===~ parseDot(v2)
    }
  }

  it should "compare non-isomorphic graph without polyvalent edge" in {
    val cases =
      Seq(
        """@a{ -> b : b; b -> c: c; c -> : b}""" -> """@a{ -> b : b; b -> c: c; c -> : a}""",
        """@a{ -> b : b; b -> c: c; c -> : a}""" -> """@a{ -> b : b; b -> c: c; c -> : a; c -> d : d}""",
        """@a{ -> b : b; b -> c: c; c -> : a}""" -> """@a{ -> b : b; b -> c: c; c -> : a; c -> d : d; d -> : a}""",
        """@a{ -> b : b; -> c : c; b -> c : c; c -> : a}""" -> """@a{ -> b : b; -> c : c; c -> b : b; c -> : a}""",
        """@a{ -> b : b; b -> c: c; c -> : a}""" -> """@a{ -> b : b; b -> c: c; c -> : a; c -> b : d}"""
      )
    for ((v1, v2) <- cases) {
      parseDot(v1) ==/~ parseDot(v2)
    }
  }

  it should "compare non-isomorphic graph with polyvalent edge" in {
    val cases =
      Seq(
        """@a{ -> b : e; b -> c: e; c -> : e}""" -> """@a{ -> b : e; b -> c: e; c -> : e; c -> d : e}""",
        """@a{ -> b : e; b -> c: e; c -> : e}""" -> """@a{ -> b : e; b -> c: e; c -> : e; c -> d : e; d -> : e}""",
        """@a{ -> b : e; -> c : e; b -> c : e; c -> : e}""" -> """@a{ -> b : e; -> c : e; c -> b : e; c -> : e}""",
        """@a{ -> b : e; b -> c: e; c -> : e}""" -> """@a{ -> b : e; b -> c: e; c -> : e; c -> b : e}"""
      )
    for ((v1, v2) <- cases) {
      parseDot(v1) ==/~ parseDot(v2)
    }
  }

  it should "Compare same cells" in {
    val cases = Seq(
      """{a; b; c}""" -> """{a; b; c}""",
      """{a; b; c}""" -> """{c; b; a}""",
      """{
        |  a
        |  b
        |  a(b: c@C) -> b(c: C)
        |  e -> f
        |}
      """.stripMargin
        ->
          """{
            | e -> f
            | a(b: c@C) -> b(c: C)
            | a
            | b
            |}
          """.stripMargin
    )
    for ((v1, v2) <- cases) {
      degrel.parseCell(v1) ===~ degrel.parseCell(v2)
    }
  }

  it should "Compare different cells" in {
    val cases = Seq(
      """{a}""" -> """{b}""",
      """{a; b}""" -> """{b; c}""",
      """{a -> b}""" -> """{b -> c}""",
      """{a -> b(c: d)}""" -> """{a -> b(c: e)}""",
      """{b(c: d); a; a -> b(c: d); e -> f}""" -> """{b(c: d); a; a -> b(c: f); e -> f}"""
    )
    for ((v1, v2) <- cases) {
      degrel.parseCell(v1) ==/~ degrel.parseCell(v2)
    }
  }
}
