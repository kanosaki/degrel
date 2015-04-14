package degrel.builtins.rewriter.math

import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class MathTest extends FlatSpec {
  val vertex = degrel.parseVertex _

  Seq(
    ("plus numbers",
      new Plus(),
      "1 + 2",
      "3")
  ).foreach({
    case (description, rw, target, expected) => {
      "Plus rewriter" should description in {
        val targetV = vertex(target)
        val expectedV = vertex(expected)
        assert(rw.rewrite(targetV).done)
        assert(targetV ===~ expectedV)
      }
    }
  })
}
