package degrel.front

import degrel.utils.PrettyPrintOptions
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class TermParserTest extends FlatSpec {
  implicit val prettyPrintOptions = PrettyPrintOptions(multiLine = true)

  val parseDot = ParserUtils.parseDot _

  val combinatorParser = Parser.vertex _

  val parboiledParser = Parser.Parboiled.vertex _

  def compareAst(code: String) = {
    val cAst = combinatorParser(code)
    val pAst = parboiledParser(code)
    if (cAst != pAst) {
      val cGraph = graphbuilder.build(cAst)
      val pGraph = graphbuilder.build(pAst)
      fail(s"${cGraph.pp} did not equals ${pGraph.pp}")
    }
  }

  "Comparing AST" should "return same vertex" in {
    compareAst("foo")
  }

  it should "return same functor" in {
    compareAst("foo(bar: baz)")
  }

  it should "return same functor which has reference" in {
    compareAst("foo@X(hoge: fuga(piyo: X))")
  }

  it should "return same rule" in {
    compareAst("a -> b + c -> (x % y)")
  }

  it should "return same abbreviated rule" in {
    compareAst("foo foo -> bar baz")
  }

  it should "parse same operator" in {
    compareAst("1 == 1")
    compareAst("2 != 2")
    compareAst("4 < 3")
    compareAst("5 > 3")
  }

  it should "return same cell" in {
    compareAst(
      """{
        | foo {
        |   bar
        | }
        |}
      """.stripMargin)
    compareAst(
      """{
        |  eq {
        |    1 { 123 == 123 }
        |    2 { 123 == 456 }
        |  }
        |
        |  neq {
        |    1 { 123 != 123 }
        |    2 { 123 != 456 }
        |  }
        |}
      """.stripMargin)
    compareAst( """foo -> {
                  |   a
                  |   a -> b
                  |}
                """.stripMargin)
  }

  it should "return same cell 2" in {
    compareAst(
      """greet(@N, @Max) -> {
        |  fin if(foo,
        |    then: {
        |      a
        |    })
        |}
      """.stripMargin)
  }

  val parsers = Seq(
    "ParboiledParser" -> parboiledParser,
    "CombinatorParser" -> combinatorParser)

  for ((name, parser) <- parsers) {

    name should "parse single vertex" in {
      val ast = parser("foo ")
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
        """.
          stripMargin)
      assert(graph ===~ expected)
    }

    it should "parse an empty cell" in {
      val ast = parser("{\n} ")
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
        """.
          stripMargin)
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
        """.
          stripMargin)
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
        """.
          stripMargin)
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
        """.
          stripMargin)
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
        """
          .stripMargin)
      assert(graph ===~ expected
      )
    }

    it should "parse a expression 3" in {
      val ast = parser(
        "a -> b + c -> (x % y)")
      val graph = graphbuilder.
        build(ast)
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
        """.
          stripMargin)
      assert(
        graph ===~ expected)
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
        """.
          stripMargin)
      assert(graph
               ===~ expected)
    }

    it should "parse a rule which generates an empty cell" in {
      val ast = parser("foo -> {}")
      val graph = graphbuilder.build(ast)
      val
      expected = parseDot(
        """@ '->' {
          |  -> foo : __lhs__
          |  -> __cell__ : __rhs__
          |}
        """.
          stripMargin)
      assert(graph ===~ expected)
    }

    it should "parse a simple cell with a vertex" in {
      val ast = parser("{a}")
      val graph = graphbuilder.build(ast)
      val expected = parseDot(
        """@ __cell__ {
          |  -> a : __item__
          |}
        """.
          stripMargin)
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
        """{
          |   a
          |   b
          |}
        """.
          stripMargin)
      val graph = graphbuilder.build(ast)
      val expected = parseDot(
        """@ __cell__ {
          |  -> a : __item__
          |  -> b : __item__
          |}
        """.
          stripMargin)
      assert(graph ===~ expected)
    }

    it should "parse a nested cell" in {
      val ast = parser(
        """{
          |  foo {
          |   hoge
          |  }, bar: homu
          |  piyo {
          |   foobar(baz)
          |  }
          |}
        """.
          stripMargin)
      val graph = graphbuilder.build(ast)
      val expected = parseDot(
        """@ __cell__ {
          |  -> foo : __item__
          |  foo -> __cell__$2 : 0
          |  foo -> homu : bar
          |  __cell__$2 -> hoge : __item__
          |  -> piyo : __item__
          |  piyo -> __cell__$3 : 0
          |  __cell__$3 -> foobar : __item__
          |  foobar -> baz : 0
          |}
        """.
          stripMargin)
      assert(graph ===~ expected)
    }

    it should "parse a simple cell" in {
      val ast = parser(
        """foo -> {
          |   a
          |   a -> b
          |}
        """.stripMargin)
      val
      graph = graphbuilder.build(ast)
      val expected = parseDot(
        """@ '->' {
          |  -> foo : __lhs__
          |  -> __cell__ : __rhs__
          |  __cell__ -> a$1 : __item__
          |  __cell__ -> '->'$2 : __rule__
          |  '->'$2 -> a$2 : __lhs__
          |  '->'$2 -> b : __rhs__
          |}
        """.
          stripMargin)
      assert(graph ===~ expected)
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
        """.
          stripMargin)
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
        """.
          stripMargin)
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
        """greet(@N, @Max) -> {
          |  fin if((N < Max),
          |    then: {
          |      println("HELLO", N)
          |      fin greet((N + 1), Max)
          |    },
          |    else: {
          |      println("DONE", N)
          |    })
          |}
        """.
          stripMargin).toGraph
    }
  }

  // Parboiled only syntaxes

  "ParboiledParser (only)" should "parse a cell edge" in {
    val ast = parboiledParser(
      """{
        |   an_edge : hogefuga
        |}
      """.
        stripMargin)
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ __cell__ {
        |  -> hogefuga : an_edge
        |}
      """.
        stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a cell edge and reference" in {
    val ast = parboiledParser(
      """{
        |   hoge@X
        |   {
        |     __piyo__: X
        |   }
        |}
      """.
        stripMargin)
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ __cell__ {
        |  -> hoge : __item__
        |  -> __cell__$2 : __item__
        |  __cell__$2 -> __ref__ : __piyo__
        |  __ref__ -> hoge : __to__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a cell pragma" in {
    val ast = parboiledParser(
      """{
        |  # foo: bar, piyo: baz
        |  hoge -> fuga
        |}
      """.
        stripMargin)
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->' : __rule__
        |  '->' -> hoge : __lhs__
        |  '->' -> fuga : __rhs__
        |  '->' -> bar : foo
        |  '->' -> baz : piyo
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }
}

