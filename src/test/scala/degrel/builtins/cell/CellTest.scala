package degrel.builtins.cell

import degrel.core.Cell
import org.scalatest.FlatSpec
import degrel.utils.TestUtils._

class CellTest extends FlatSpec {
  val MAX_STEP = 100

  def vertex(s: String) = degrel.parseVertex(s).asHeader

  "Send message operator" should "add vertex to a cell" in {
    val targetV = vertex("{} ! foo")
    val expectedV = vertex("{foo}")
    val rw = new SendMessage()
    assert(rw.rewrite(targetV, Cell()).done)
    assert(targetV ===~ expectedV)
  }
}
