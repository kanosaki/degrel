package degrel.utils

import degrel.core.Element
import org.scalatest.exceptions.TestFailedException

import scala.language.implicitConversions


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

  def assertElementSet(vA: Set[_ <: Element], vB: Set[_ <: Element]) = {
    val vAmapped = vA.map(new ElementEqualityAdapter(_))
    val vBmapped = vB.map(new ElementEqualityAdapter(_))
    if (vAmapped != vBmapped) {
      throw new TestFailedException(s"$vA not equals $vB", 0)
    }
  }
}
