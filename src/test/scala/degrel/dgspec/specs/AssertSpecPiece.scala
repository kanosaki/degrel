package degrel.dgspec.specs

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec.{NextPiece, SpecContext, SpecFactory, SpecPiece}
import degrel.utils.TestUtils._

class AssertSpecPiece(root: String) extends SpecPiece {
  val expectedCell = degrel.parseVertex(root)

  override def evaluate(ctx: SpecContext): NextPiece = {
    assert(ctx.root ===~ expectedCell)
    NextPiece.Continue
  }
}

object AssertSpecPiece {
  def decode(node: JsonNode)
            (implicit specFactory: SpecFactory): AssertSpecPiece = {
    require(node.isTextual)
    new AssertSpecPiece(node.asText)
  }
}
