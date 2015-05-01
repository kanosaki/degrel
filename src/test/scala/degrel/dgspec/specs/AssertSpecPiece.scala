package degrel.dgspec.specs

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec.{NextPiece, SpecContext, SpecFactory, SpecPiece}
import degrel.utils.TestUtils._

/**
 * 指定されたデータと現在の`SpecContext`を比較し，
 * Assertion を実行します
 */
class AssertSpecPiece(root: String) extends SpecPiece {
  val expectedCell = degrel.parseVertex(root)

  override def evaluate(ctx: SpecContext): NextPiece = {
    //degrel.visualize.showAndWait(ctx.root)
    try {
      assert(ctx.root ===~ expectedCell)
    } catch {
      case ex: Throwable => throw ex
    }
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
