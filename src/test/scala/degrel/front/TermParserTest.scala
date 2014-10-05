package degrel.front

import org.scalatest.FlatSpec

class TermParserTest extends FlatSpec {
  val parser = new TermParser()

  it should "parse empty graph" in {
    val ast = parser("")
    val graph = ast.toGraph()
  }

  it should "parse single vertex" in {
    val ast = parser(" foo ")
    val graph = ast.toGraph()
  }

  it should "parse an empty cell" in {
    val ast = parser(" {\n} ")
    val graph = ast.toGraph()
  }

  it should "parse a rule" in {
    val ast = parser("a \n\t-> b")
    val graph = ast.toGraph()
  }

  it should "parse a expression" in {
    val ast = parser("a -> b + c -> (x % y)")
    val graph = ast.toGraph()
    println(graph)
  }

  it should "parse a rule which generates an empty cell" in {
    val ast = parser("foo -> {}")
    val graph = ast.toGraph()
  }

  it should "parse a simple cell with a vertex" in {
    val ast = parser("{a}")
    val graph = ast.toGraph()
  }

  it should "parse a simple cell with a rule" in {
    val ast = parser("{a -> b}")
    val graph = ast.toGraph()
  }

  it should "parse a two element cell" in {
    val ast = parser(
      """ {
        |   a
        |   b
        | }
      """.stripMargin)
    val graph = ast.toGraph()
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
  }

  it should "parse a import stetement" in {
    val ast = parser("import foobar.baz.hoge")
    val graph = ast.toGraph()
  }

  it should "parse a import stetement with 'as'" in {
    val ast = parser("import foobar.baz.hoge as hogehoge")
    val graph = ast.toGraph()
  }

  it should "parse a import with 'from'" in {
    val ast = parser("from foobar.baz import hoge")
    val graph = ast.toGraph()
  }

  it should "parse a import with 'from' and 'as'" in {
    val ast = parser("from foobar.baz import hoge as hogehoge")
    val graph = ast.toGraph()
  }

  it should "parse a multi imports" in {
    val ast = parser("import foo.bar, hoge.fuga")
    val graph = ast.toGraph()
  }

  it should "parse a multi imports with 'from'" in {
    val ast = parser("from piyo import foo, bar")
    val graph = ast.toGraph()
  }

  it should "throw CodeError when multi import with 'as'" in {
    intercept[SyntaxError] {
      parser("import foo, bar as hoge")
    }
  }

  it should "parse a cell with import statement" in {
    val ast = parser(
      """foo -> {
        |   from hoge.fuga import piyo as foo
        |   foo(hoge: fuga)
        |   bar -> baz
        | }
      """.stripMargin)
    val graph = ast.toGraph()
  }

  it should "parse complicated cell" in {
    val ast = parser(
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
    val graph = ast.toGraph()
  }
}

