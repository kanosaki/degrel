package degrel.primitives.rewriter.cell

import degrel.engine.Driver
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class CellTest extends FlatSpec {
  val MAX_STEP = 100

  def vertex(s: String) = degrel.parseVertex(s).asHeader

  "Send message operator" should "add vertex to a cell" in {
    val targetV = vertex("{} ! foo")
    val expectedV = vertex("{foo}")
    val rw = new SendMessage()
    assert(rw.rewrite(targetV, Driver()).done)
    assert(targetV ===~ expectedV)
  }
}
