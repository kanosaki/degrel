package degrel.engine.rewriting

import degrel.front.ParserUtils
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class RewriterTest extends FlatSpec {
  val vertex = degrel.parseVertex _
  val parseDot = ParserUtils.parseDot _

  def rewriter(s: String) = Rewriter(vertex(s).asRule)

  Seq(
    ("Spawn cell within a functor",
      "foo(bar: baz)",
      "foo(bar: @X) -> child({foobar(X)})",
      "child({__base__: {foobar(__ref__(__to__: _))}; foobar(baz)})"),
    ("Spawn cell with loop",
      "foo",
      "foo -> {hoge@X(fuga: X)}",
      "{__base__: {hoge@Y(fuga: Y)}; hoge@X(fuga: X)}"),
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
    ("Build a functor with nested capturing 2",
      "a(b: b, c: c)",
      "a@A(b: _@B, c: _@C) -> foo(a: A, b: B, c: C)",
      "foo(a: a(b: b@B, c: c@C), b: B, c: C)"),
    ("Build a functor with an others rule",
      "a(b: b, c: c)",
      "a@A(_:@Others) -> foo(hoge: fuga, _:Others)",
      "foo(hoge: fuga, b: b, c: c)"),
    ("Build a functor with copying others rule",
      "a(b: b, c: c)",
      "a@A(_:@Others) -> A(hoge: fuga, _:Others)",
      "a(hoge: fuga, b: b, c: c)"),
    ("Delete edges using others edge.",
      "a(b: b, c: c)",
      "a@A(b: b, _:@Others) -> A(b: d, _:Others)",
      "a(b: d, c: c)"),
    ("Move all edges by others edge.",
      "a(b: b, c: c)",
      "a(_:@Others) -> b(_:Others)",
      "b(b: b, c: c)"),
    ("Delete edges using others edge. 2",
      "a(b: b, c: c, d: e)",
      "a@A(b: b, d: e, _:@Others) -> A(b: d, _:Others)",
      "a(b: d, c: c)"),
    ("Import all edges by include edge",
      "a(b: b)",
      "a@A(b: b) -> c(c: c, _:A)",
      "c(b: b, c: c)"),
    ("Import all edges by include edge 2",
      "a(b: b, x: y)",
      "a@A(b: b) -> c(c: c, _:A)",
      "c(b: b, c: c, x: y)"),
    ("Build a vertex within a cell",
      "{a}",
      "{a} -> {b; c -> d}",
      "{__base__: {b; c -> d}; b}")
  ).foreach({
    case (description, target, rule, expected) => {
      it should description in {
        val targetV = vertex(target)
        val rw = rewriter(rule)
        val expectedV = vertex(expected)
        val rc = RewritingTarget.alone(targetV, null)
        rw.rewrite(rc) match {
          case RewriteResult.Write(_, v) => {
            assert(v ===~ expectedV)
          }
          case _ => fail("not match")
        }
      }
    }
  })
}
