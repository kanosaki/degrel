package degrel.builtins.cell

import degrel.core.Cell
import degrel.engine.Driver
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class CellTest extends FlatSpec {
  val MAX_STEP = 100
  val vertex = degrel.parseVertex _
  def toCell(s: String) = degrel.parseVertex(s).asCell

  "Send message operator" should "add vertex to a cell" in {
    val targetV = vertex("{} ! foo")
    val expectedV = vertex("{foo}")
    val rw = new SendMessage()
    assert(rw.rewrite(targetV, Cell()).done)
    assert(targetV ===~ expectedV)
  }
}
