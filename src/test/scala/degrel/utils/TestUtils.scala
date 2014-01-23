package degrel.utils

import scala.language.implicitConversions
import degrel.core.{Element, Vertex}
import org.scalatest.exceptions.TestFailedException


object TestUtils {

  class TestUtilsElementExtensions(elem: Element) {

    def ===~(other: Element) = {
      if (elem =/~ other) {
        throw new TestFailedException(s"$elem did not equal $other", 0)
      }
      true
    }

    def ==/~(other: Element) = {
      if (elem ==~ other) {
        throw new TestFailedException(s"$elem did equal $other", 0)
      }
      true
    }
  }

  implicit def testUtilsVertexExtension(elem: Element) = new TestUtilsElementExtensions(elem)

  def assertVertices(vA: Set[Vertex], vB: Set[Vertex]) = {
    val vAmapped = vA.map(new VertexEqualityAdapter(_))
    val vBmapped = vB.map(new VertexEqualityAdapter(_))
    if (vAmapped != vBmapped) {
      throw new TestFailedException(s"$vA not equals $vB", 0)
    }
  }
}
