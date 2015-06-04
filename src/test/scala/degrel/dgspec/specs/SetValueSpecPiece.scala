package degrel.dgspec.specs

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec.{SpecFactory, NextPiece, SpecContext, SpecPiece}

/**
 * 現在のコンテキストへ値をセットします
 */
case class SetValueSpecPiece(rootCell: String) extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    val rootCellValue = degrel.parseVertex(rootCell)
    ctx.root = rootCellValue
    NextPiece.Continue
  }
}

object SetValueSpecPiece {
  def decode(node: JsonNode)
            (implicit specFactory: SpecFactory): SetValueSpecPiece = {
    require(node.isTextual)
    new SetValueSpecPiece(node.asText)
  }
}
