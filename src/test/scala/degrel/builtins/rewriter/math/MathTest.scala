package degrel.builtins.rewriter.math

import degrel.engine.Driver
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class MathTest extends FlatSpec {
  def vertex(s: String) = degrel.parseVertex(s).asHeader

  "Plus rewriter" should "plus numbers" in {
    val targetV = vertex("1 + 2")
    val expectedV = vertex("3")
    val rw = new Plus()
    assert(rw.rewrite(targetV, Driver()).done)
    assert(targetV ===~ expectedV)
  }

  it should "not works for non-numbers" in {
    val targetV = vertex("1 + x")
    val expectedV = vertex("1 + x")
    val rw = new Plus()
    assert(!rw.rewrite(targetV, Driver()).done)
    assert(targetV ===~ expectedV)
  }

  it should "be able to plus negative numbers in right hand" in {
    val targetV = vertex("1 + (-3)")
    val expectedV = vertex("-2")
    val rw = new Plus()
    assert(rw.rewrite(targetV, Driver()).done)
    assert(targetV ===~ expectedV)
  }

  it should "be able to plus negative numbers in left hand" in {
    val targetV = vertex("(-3) + 1")
    val expectedV = vertex("-2")
    val rw = new Plus()
    assert(rw.rewrite(targetV, Driver()).done)
    assert(targetV ===~ expectedV)
  }

  it should "be able to plus negative numbers in both hands" in {
    val targetV = vertex("(-3) + (-2)")
    val expectedV = vertex("-5")
    val rw = new Plus()
    assert(rw.rewrite(targetV, Driver()).done)
    assert(targetV ===~ expectedV)
  }

}
