package degrel.engine.rewriting

import degrel.front.ParserUtils
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class RewriterTest extends FlatSpec {
  val vertex = degrel.parseVertex _

  def rewriter(s: String) = new Rewriter(vertex(s).asRule)

  val parseDot = ParserUtils.parseDot _

  Seq(
    ("Build a simple vertex",
      "a",
      "a -> b",
      "b"),
    ("Build a simple functor",
      "a(b: c)",
      "a(b: c) -> d(e: f)",
      "d(e: f)"),
    ("Build a functor with a variable",
      "a(b: c)",
      "a(b: _@X) -> d(e: X)",
      "d(e: c)"),
    ("Build a functor with a variable and its children",
      "a(b: c(x: y, p: q))",
      "a(b: _@X(x: y)) -> d(e: X)",
      "d(e: c(x: y, p: q))"),
    ("Build a functor with variables",
      "a(b: c(x: y, p: q))",
      "a(b: _(x: _@Y, p: _@Q)) -> d(e: Y, f: Q)",
      "d(e: y, f: q)"),
    ("Build a functor with nested capturing",
      "a(b: c(x: y(foo: bar), hoge: fuga))",
      "a(b: _@C(x: _@Y(foo: bar), hoge: fuga)) -> d(e: Y, c: C)",
      "d(e: y@Y(foo: bar), c: c(x: Y, hoge: fuga))"),
    ("Build a vertex within a cell",
      "{a}",
      "{a} -> {b}",
      "{b}")
  ).foreach({
    case (description, target, rule, expected) => {
      it should description in {
        val targetV = vertex(target)
        val rw = rewriter(rule)
        val expectedV = vertex(expected)
        val actual = rw.build(targetV)
        assert(actual.get ===~ expectedV)
      }
    }
  })
}
