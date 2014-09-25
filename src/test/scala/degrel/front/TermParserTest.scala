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
}

