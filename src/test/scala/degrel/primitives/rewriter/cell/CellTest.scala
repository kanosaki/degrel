package degrel.primitives.rewriter.cell

import degrel.core.Label
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
    val driver = Driver(targetV.thruSingle(Label.E.lhs).asCell)
    val res = rw.rewrite(driver, targetV)
    res.exec(driver)
    assert(driver.header ===~ expectedV)
  }
}
