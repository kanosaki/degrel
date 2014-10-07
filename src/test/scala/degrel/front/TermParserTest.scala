package degrel.front

import degrel.core.Cell
import org.scalatest.FlatSpec
import degrel.utils.TestUtils._

class TermParserTest extends FlatSpec {
  val parser = new TermParser()
  val parseDot = ParserUtils.parseDot _

  it should "parse empty graph" in {
    val ast = parser("")
    val graph = ast.toGraph()
    assert(graph ===~ Cell())
  }

  it should "parse single vertex" in {
    val ast = parser(" foo ")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        | -> foo : __item__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse an empty cell" in {
    val ast = parser(" {\n} ")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        | -> __cell__ : __item__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a rule" in {
    val ast = parser("a \n\t-> b")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->' : __rule__
        |  '->' -> a : __lhs__
        |  '->' -> b : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a expression 1" in {
    val ast = parser("a -> b + c")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->'$1 : __rule__
        |  '->'$1 -> a : __lhs__
        |  '->'$1 -> '+' : __rhs__
        |  '+' -> b : __lhs__
        |  '+' -> c : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a expression 2" in {
    val ast = parser("a + b -> c")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->'$1 : __rule__
        |  '->'$1 -> '+' : __lhs__
        |  '->'$1 -> c : __rhs__
        |  '+' -> a : __lhs__
        |  '+' -> b : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a expression 3" in {
    val ast = parser("a -> b + c -> (x % y)")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->'$1 : __rule__
        |  '->'$1 -> a : __lhs__
        |  '->'$1 -> '->'$2 : __rhs__
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

  it should "parse a rule which generates an empty cell" in {
    val ast = parser("foo -> {}")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->' : __rule__
        |  '->' -> foo : __lhs__
        |  '->' -> __cell__ : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a simple cell with a vertex" in {
    val ast = parser("{a}")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> __cell__ : __item__
        |  __cell__ -> a : __item__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  it should "parse a simple cell with a rule" in {
    val ast = parser("{a -> b}")
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> __cell__ : __item__
        |  __cell__ -> '->' : __rule__
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
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> __cell__ : __item__
        |  __cell__ -> a : __item__
        |  __cell__ -> b : __item__
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
    val graph = ast.toGraph()
    val expected = parseDot(
      """@ __cell__ {
        |  -> '->'$1 : __rule__
        |  '->'$1 -> foo : __lhs__
        |  '->'$1 -> __cell__ : __rhs__
        |  __cell__ -> a$1 : __item__
        |  __cell__ -> '->'$2 : __rule__
        |  '->'$2 -> a$2 : __lhs__
        |  '->'$2 -> b : __rhs__
        |}
      """.stripMargin)
    assert(graph ===~ expected)
  }

  // TODO: Add test for imports (not only for parsing but for its behavior)

  it should "parse a import stetement" in {
    parser("import foobar.baz.hoge")
  }

  it should "parse a import stetement with 'as'" in {
    parser("import foobar.baz.hoge as hogehoge")
  }

  it should "parse a import with 'from'" in {
    parser("from foobar.baz import hoge")
  }

  it should "parse a import with 'from' and 'as'" in {
    parser("from foobar.baz import hoge as hogehoge")
  }

  it should "parse a multi imports" in {
    parser("import foo.bar, hoge.fuga")
  }

  it should "parse a multi imports with 'from'" in {
    parser("from piyo import foo, bar")
  }

  it should "throw CodeError when multi import with 'as'" in {
    intercept[SyntaxError] {
      parser("import foo, bar as hoge")
    }
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
}

