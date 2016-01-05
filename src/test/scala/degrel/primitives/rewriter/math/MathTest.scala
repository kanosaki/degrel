package degrel.primitives.rewriter.math

import degrel.core.{Vertex, VertexHeader}
import degrel.engine.rewriting.{RewritingTarget, RewriteResult, Rewriter}
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class MathTest extends FlatSpec {
  def vertex(s: String) = degrel.parseVertex(s).asHeader

  def execRewriter(rw: Rewriter, targetV: VertexHeader, expectedV: Vertex) = {
    val rc = RewritingTarget.alone(targetV, null)
    val res = rw.rewrite(rc) match {
      case RewriteResult.Write(t, v) => {
        assert(v ===~ expectedV)
      }
      case _ => fail()
    }
  }

  "Plus rewriter" should "plus numbers" in {
    val targetV = vertex("1 + 2")
    val expectedV = vertex("3")
    val rw = new IntAdd()
    execRewriter(rw, targetV, expectedV)
  }

  it should "not works for non-numbers" in {
    val targetV = vertex("1 + x")
    val rw = new IntAdd()
    val rc = RewritingTarget.alone(targetV, null)
    val res = rw.rewrite(rc) match {
      case RewriteResult.Nop => {
      }
      case _ => fail()
    }
  }

  it should "be able to plus negative numbers in right hand" in {
    val targetV = vertex("1 + (-3)")
    val expectedV = vertex("-2")
    val rw = new IntAdd()
    execRewriter(rw, targetV, expectedV)
  }

  it should "be able to plus negative numbers in left hand" in {
    val targetV = vertex("(-3) + 1")
    val expectedV = vertex("-2")
    val rw = new IntAdd()
    execRewriter(rw, targetV, expectedV)
  }

  it should "be able to plus negative numbers in both hands" in {
    val targetV = vertex("(-3) + (-2)")
    val expectedV = vertex("-5")
    val rw = new IntAdd()
    execRewriter(rw, targetV, expectedV)
  }

}
