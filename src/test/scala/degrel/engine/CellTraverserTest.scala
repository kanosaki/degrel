package degrel.engine

import org.scalatest.FlatSpec

class CellTraverserTest extends FlatSpec {

  it should "traverse rewrite target cell elements" in {
    val cell = degrel.parseVertex(
      """
        |{
        |  a
        |  b
        |  c -> d
        |}
      """.stripMargin).toCell
    val result = CellTraverser(cell, null).toSeq
    assert(result.size === 2)
  }
}
