package degrel.engine

import degrel.core.Cell
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class PrarapatTest extends FlatSpec {

  def toCell(s: String) = degrel.parseVertex(s).asInstanceOf[Cell]

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
    assert(pra.step(), "'a -> b' to a haven't applied.")
    assert(!pra.step(), "haven't finished")
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
    assert(pra.step(), "'a -> b' to a(1) haven't applied.")
    assert(pra.step(), "'a -> b' to a(2) haven't applied.")
    assert(!pra.step(), "haven't finished")
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
    (0 to 4).foreach(n => {
      assert(pra.step(), s"Rule apply $n")
    })
    assert(!pra.step(), "haven't finished")
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
