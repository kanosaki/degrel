package degrel.utils

import scala.language.implicitConversions
import degrel.core.Element


object TestUtils {
  class TestUtilsElementExtensions(elem: Element) {

    def ===~(other: Element) = {
      if(elem =/~ other) {
        throw new AssertionError(s"$elem did not equal $other")
      }
      true
    }

    def ==/~(other: Element) = {
      if(elem ==~ other) {
        throw new AssertionError(s"$elem did not equal $other")
      }
      true
    }
  }
  implicit def testUtilsVertexExtension(elem: Element) = new TestUtilsElementExtensions(elem)
}
