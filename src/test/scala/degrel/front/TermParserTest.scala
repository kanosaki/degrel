package degrel.front

import degrel.graphbuilder
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class TermParserTest extends FlatSpec {
  val parser = Parser.vertex _
  val parseDot = ParserUtils.parseDot _

  it should "parse single vertex" in {
    val ast = parser(" foo ")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@foo{}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse single functor" in {
    val ast = parser("foo(bar: baz)")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@foo{
        | -> baz: bar
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse an empty cell" in {
    val ast = parser(" {\n} ")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ __cell__ {
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a rule" in {
    val ast = parser("a ->\n\t b")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@'->'{
        |  -> a : __lhs__
        |  -> b : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a quoted vertex" in {
    val ast = parser("'->'(__lhs__: a, __rhs__: b)")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@'->'{
        |  -> a : __lhs__
        |  -> b : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "construct a functor with one capture" in {
    val ast = parser("foo@X(bar: baz, hoge: fuga(piyo: X))")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ foo{
        | -> baz : bar
        | -> fuga : hoge
        | fuga -> : piyo
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "construct a rule with one capture" in {
    val ast = parser("foo@X -> hoge(fuga: X)")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@'->'{
        |  -> foo : __lhs__
        |  -> hoge : __rhs__
        |  hoge -> '__ref__' : fuga
        |  '__ref__' -> 'foo' : __to__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a expression 1" in {
    val ast = parser("a -> b + c")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ '->' {
        |  -> a : __lhs__
        |  -> '+' : __rhs__
        |  '+' -> b : __lhs__
        |  '+' -> c : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a expression 2" in {
    val ast = parser("a + b -> c")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ '->' {
        |  -> '+' : __lhs__
        |  -> c : __rhs__
        |  '+' -> a : __lhs__
        |  '+' -> b : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a expression 3" in {
    val ast = parser("a -> b + c -> (x % y)")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ '->' {
        |  -> a : __lhs__
        |  -> '->'$2 : __rhs__
        |  '->'$2 -> '+' : __lhs__
        |  '+' -> b : __lhs__
        |  '+' -> c : __rhs__
        |  '->'$2 -> '%' : __rhs__
        |  '%' -> x : __lhs__
        |  '%' -> y : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a rule definition rule" in {
    val ast = parser("(@Lhs == @Rhs) -> op.equals(Lhs, Rhs)")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ '->' {
        |  -> '==' : __lhs__
        |  -> 'op.equals' : __rhs__
        |  '==' -> '_'$1 : __lhs__
        |  '==' -> '_'$2 : __rhs__
        |  'op.equals' -> __ref__$1 : 0
        |  __ref__$1 -> '_'$1 : __to__
        |  'op.equals' -> __ref__$2 : 1
        |  __ref__$2 -> '_'$2 : __to__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a rule definition rule in cell" in {
    val ast = parser("{(@Lhs == @Rhs) -> op.equals(Lhs, Rhs)}")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->' : __rule__
        |  '->' -> '==' : __lhs__
        |  '->' -> 'op.equals' : __rhs__
        |  '==' -> '_'$1 : __lhs__
        |  '==' -> '_'$2 : __rhs__
        |  'op.equals' -> __ref__$1 : 0
        |  __ref__$1 -> '_'$1 : __to__
        |  'op.equals' -> __ref__$2 : 1
        |  __ref__$2 -> '_'$2 : __to__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a rule which generates an empty cell" in {
    val ast = parser("foo -> {}")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ '->' {
        |  -> foo : __lhs__
        |  -> __cell__ : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a simple cell with a vertex" in {
    val ast = parser("{a}")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ __cell__ {
        |  -> a : __item__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a simple cell with a rule" in {
    val ast = parser("{a -> b}")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->' : __rule__
        |  '->' -> a : __lhs__
        |  '->' -> b : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a two element cell" in {
    val ast = parser(
      """ {
        |   a
        |   b
        | }
      """.stripMargin)
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ __cell__ {
        |  -> a : __item__
        |  -> b : __item__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a simple cell" in {
    val ast = parser(
      """
        |foo -> {
        |   a
        |   a -> b
        |}
      """.stripMargin)
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ '->' {
        |  -> foo : __lhs__
        |  -> __cell__ : __rhs__
        |  __cell__ -> a$1 : __item__
        |  __cell__ -> '->'$2 : __rule__
        |  '->'$2 -> a$2 : __lhs__
        |  '->'$2 -> b : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a cell with import statement" in {
    parser(
      """foo -> {
        |   from hoge.fuga import piyo as foo
        |   foo(hoge: fuga)
        |   bar -> baz
        | }
      """.stripMargin)
  }

  it should "parse complicated cell" in {
    parser(
      """foo -> {
        |   from hoge.fuga import piyo as foo
        |   defop >>= -11 right
        |   hogehoge -> {
        |     foo -> bar
        |   }
        |   john = doe(foo: bar)
        |   ten = 1 + 2 +
        |     3 + 4
        |   InnerClass(arg1: hoge, arg2: fuga) -> {
        |     foo = piyo(foo: hoge, bar: fuga)
        |     fin poyo(result: foo)
        |   }
        | }
      """.stripMargin)
  }

  it should "Restore abbreviated edges." in {
    val ast = parser("foo(bar, baz, hoge, x: piyo)")
    val expectedAst = parser("foo(0: bar, 1: baz, 2: hoge, x: piyo)")
    val graph = graphbuilder.build(ast)
    val expected = graphbuilder.build(expectedAst)
    assert(graph ===~ expected)
  }

  it should "throw SyntaxError if abbreviated edge appears after non-abbreviated edges." in {
    intercept[SyntaxError] {
      parser("foo(bar, baz, hoge, x: piyo, baz)")
    }
  }

  it should "parse abbreviated label edges" in {
    val ast = parser("foo(baz, hoge)")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@foo{
        | -> baz: 0
        | -> hoge: 1
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse brace abbreviated edges" in {
    val ast = parser("foo foo: baz, bar: hoge")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@foo{
        | -> baz: foo
        | -> hoge: bar
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse brace and edge label abbreviated edges" in {
    val ast = parser("foo baz, hoge fuga")
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@foo{
        | -> baz: 0
        | -> hoge: 1
        | hoge -> fuga: 0
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "brace abbr, and contains binop expressions" in {
    val graph = parser("foo (foo + bar)").toGraph
    val expected = parser("foo(0: '+'(__lhs__: foo, __rhs__: bar))").toGraph
    assert(graph ===~ expected)
  }

  it should "brace abbr, and contains binop expressions without braces" in {
    val graph = parser("foo foo -> bar").toGraph
    val expected = parser("foo(foo) -> bar").toGraph
    assert(graph ===~ expected)
  }

  it should "brace abbr, and contains binop expressions without braces 2" in {
    val graph = parser("foo foo -> bar baz").toGraph
    val expected = parser("foo(foo) -> bar(baz)").toGraph
    assert(graph ===~ expected)
  }

  it should "brace abbr, and contains binop expressions, and captureing without braces 2" in {
    val graph = parser("foo foo, @Bar -> Bar baz").toGraph
    val expected = parser("foo(foo, @Bar) -> Bar(baz)").toGraph
    assert(graph ===~ expected)
  }

  it should "brace abbr, three atoms" in {
    val graph = parser("foo bar baz").toGraph
    val expected = parser("foo(bar(baz))").toGraph
    assert(graph ===~ expected)
  }

  it should "brace abbr, with cell" in {
    val graph = parser("foo {hoge}").toGraph
    val expected = parser("foo(0: __cell__(__item__: hoge))").toGraph
    assert(graph ===~ expected)
  }

  it should "brace abbr, with cell 2" in {
    val ast = parser("foo {hoge}, bar: {foo}")
    val graph = ast.toGraph
    val expected = parser("foo(0: __cell__(__item__: hoge), bar: __cell__(__item__: foo))").toGraph
    assert(graph ===~ expected)
  }

  it should "parse vertex with newline in functor" in {
    val graph = parser(
      """foo(
        |   bar,
        |   baz
        |)""".stripMargin).toGraph
    val expected = parser("foo(bar, baz)").toGraph
    assert(graph ===~ expected)
  }

  it should "parse vertex with newline in functor with abbreviated edges" in {
    val graph = parser(
      """foo bar,
        |   baz""".stripMargin).toGraph
    val expected = parser("foo(bar, baz)").toGraph
    assert(graph ===~ expected)
  }

  it should "parse multi argument rule" in {
    val graph = parser(
      """
        |greet(@N, @Max) -> {
        |  fin if((N < Max),
        |    then: {
        |      println("HELLO", N)
        |      fin greet((N + 1), Max)
        |    },
        |    else: {
        |      println("DONE", N)
        |    })
        |}
      """.stripMargin).toGraph
//    val expected = parser("f").toGraph
//    assert(graph ===~ expected)
  }
}

