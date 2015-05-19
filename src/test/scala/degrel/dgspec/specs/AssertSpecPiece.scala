package degrel.dgspec.specs

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec.{NextPiece, SpecContext, SpecFactory, SpecPiece}
import degrel.utils.TestUtils._

/**
 * 指定されたデータと現在の`SpecContext`を比較し，
 * Assertion を実行します
 */
class RootAssertSpecPiece(root: String) extends SpecPiece {
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

class OutputAssertSpecPiece(watch: String, expected: String) extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {

    NextPiece.Continue
  }
}

object AssertSpecPiece {
  def decode(node: JsonNode)
            (implicit specFactory: SpecFactory): SpecPiece = {
    if (node.isTextual) {
      new RootAssertSpecPiece(node.asText)
    } else if (node.isObject) {
      val assertTypeNode = node.get("type")
      if (assertTypeNode == null || !assertTypeNode.isTextual) {
        throw new RuntimeException(s"`type` field required and it must be text. (in $node)")
      }
      assertTypeNode.asText() match {
        case "stdout" => new OutputAssertSpecPiece("stdout", node.get("text").asText())
        case els => throw new RuntimeException(s"Unsupported assert type $els")
      }
    } else {
      throw new RuntimeException("Unsupported assert type")
    }
  }
}
