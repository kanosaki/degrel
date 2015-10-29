package degrel.primitives.rewriter.cell

import degrel.core.Label
import degrel.engine.LocalDriver
import degrel.engine.rewriting.RewritingTarget
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class CellTest extends FlatSpec {
  val MAX_STEP = 100

  def vertex(s: String) = degrel.parseVertex(s).asHeader

  "Send message operator" should "add vertex to a cell" in {
    val targetV = vertex("{} ! foo").asHeader
    val expectedV = vertex("{foo}")
    val rw = new SendMessage()
    val driver = LocalDriver(targetV.thruSingle(Label.E.lhs).asCell)
    val rc = new RewritingTarget(targetV, targetV, driver)
    val res = rw.rewrite(rc)
    res.exec(driver)
    assert(driver.header ===~ expectedV)
  }
}
