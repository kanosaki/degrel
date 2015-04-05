package degrel.engine

import degrel.core.Cell
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class DriverTest extends FlatSpec {

  val MAX_STEP = 100

  def toCell(s: String) = degrel.parseVertex(s).asInstanceOf[Cell]

  it should "Do nothing for empty cell" in {
    val cell = toCell("{}")
    val cd = new Driver(cell)
    assert(!cd.step())
  }

  Seq(
    ("Send message to a cell",
      """{
        | child({})
        |
        | (__cell__(_:@Others) ! @X) ->  __cell__(__item__: @X, _:Others)
        | child(@C) -> C ! hoge(fuga: foobar)
        |}""".stripMargin,
      """{
        | child({hoge(fuga: foobar)})
        |
        | (__cell__(_:@Others) ! @X) ->  __cell__(__item__: @X, _:Others)
        | child(@C) -> C ! hoge(fuga: foobar)
        |}
      """.stripMargin),

    ("rewrite vertex with nested capturing",
      """{
        | a(b: b, c: c, done: false)
        | a@A(b: _@B, c: _@C, done: false) -> foo(a: A(b: B, c: C), b: B, c: C, done: true)
        |}""".stripMargin,
      """{
        | foo(a: a(b: b@B, c: c@C), b: B, c: C, done: true)
        | a@A(b: _@B, c: _@C, done: false) -> foo(a: A(b: B, c: C), b: B, c: C, done: true)
        |}
      """.stripMargin),

    ("Rewrite a vertex in single step.",
      """{
        | a
        | a -> b
        |}""".stripMargin,
      """{
        | b
        | a -> b
        |}
      """.stripMargin),

    ("Rewrite multi vertices with a rule",
      """{
        | a
        | a
        | a -> b
        |}
      """.stripMargin,
      """{
        | b
        | b
        | a -> b
        |}
      """.stripMargin),

    ("Rewrite multi vertices with multi rules each other.",
      """{
        | a
        | a
        | c
        | c
        | a -> b
        | c -> d
        |}""".stripMargin,
      """{
        | b
        | b
        | d
        | d
        | a -> b
        | c -> d
        |}
      """.stripMargin)
  ) foreach {
    case (description, before, after) => {
      it should description in {
        val beforeCell = toCell(before)
        val afterCell = toCell(after)
        val cd = new Driver(beforeCell)
        cd.stepUntilStop(MAX_STEP)
        assert(cd.cell ===~ afterCell)
      }
    }
  }
}
