package degrel.utils

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import degrel.utils.IterableExtensions._
import org.scalatest.events.TestFailed

class IterableExtensionsTest extends FlatSpec with MockFactory {
  "findFirst" should "return first item which select by predicate" in {
    val f = Seq(1, 2, 3).findFirst { i =>
      if (i % 2 == 0) {
        Some(i * 2)
      } else {
        None
      }
    }

    assert(f === Some(4))
  }
}
