package degrel.engine

import degrel.core.Cell
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class PrarapatTest extends FlatSpec {

  def toCell(s: String) = degrel.parseVertex(s).asInstanceOf[Cell]

  it should "rewrite vertex with nested capturing" in {
    val cell = toCell(
      """{
        | a(b: b, c: c, done: false)
        | a@A(b: _@B, c: _@C, done: false) -> foo(a: A(b: B, c: C), b: B, c: C, done: true)
        |}""".stripMargin)
    val pra = new Praparat(cell)
    pra.stepUntilStop(100)
    val after = toCell(
      """{
        | foo(a: a(b: b@B, c: c@C), b: B, c: C, done: true)
        | a@A(b: _@B, c: _@C, done: false) -> foo(a: A(b: B, c: C), b: B, c: C, done: true)
        |}
      """.stripMargin)
    assert(pra.cell ===~ after)
  }

  it should "Do nothing for empty cell" in {
    val cell = toCell("{}")
    val pra = new Praparat(cell)
    assert(!pra.step())
  }

  it should "Rewrite a vertex in single step." in {
    val cell = toCell(
      """{
        | a
        | a -> b
        |}""".stripMargin)
    val pra = new Praparat(cell)
    pra.stepUntilStop()
    val after = toCell(
      """{
        | b
        | a -> b
        |}
      """.stripMargin)
    assert(pra.cell ===~ after)
  }

  it should "Rewrite multi vertices with a rule" in {
    val cell = toCell(
      """{
        | a
        | a
        | a -> b
        |}""".stripMargin)
    val pra = new Praparat(cell)
    pra.stepUntilStop()
    val after = toCell(
      """{
        | b
        | b
        | a -> b
        |}
      """.stripMargin)
    assert(pra.cell ===~ after)
  }

  it should "Rewrite multi vertices with multi rules each other." in {
    val cell = toCell(
      """{
        | a
        | a
        | c
        | c
        | a -> b
        | c -> d
        |}""".stripMargin)
    val pra = new Praparat(cell)
    pra.stepUntilStop()
    val after = toCell(
      """{
        | b
        | b
        | d
        | d
        | a -> b
        | c -> d
        |}
      """.stripMargin)
    assert(pra.cell ===~ after)
  }

}
