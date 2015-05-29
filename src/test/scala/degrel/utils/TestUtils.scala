package degrel.utils

import degrel.core.Element
import org.scalatest.exceptions.TestFailedException

import scala.language.implicitConversions


object TestUtils {

  implicit def testUtilsVertexExtension(elem: Element): TestUtilsElementExtensions = new TestUtilsElementExtensions(elem)

  def assertElementSet(vA: Set[_ <: Element], vB: Set[_ <: Element]) = {
    val vAmapped = vA.map(new ElementEqualityAdapter(_))
    val vBmapped = vB.map(new ElementEqualityAdapter(_))
    if (vAmapped != vBmapped) {
      throw new TestFailedException(s"$vA not equals $vB", 0)
    }
  }

  class TestUtilsElementExtensions(elem: Element) {
    implicit val ppOpt = PrettyPrintOptions(multiLine = true)

    def ===~(other: Element) = {
      if (elem =/~ other) {
        throw new TestFailedException(s"${elem.pp} did not equal ${other.pp}", 0)
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
}
