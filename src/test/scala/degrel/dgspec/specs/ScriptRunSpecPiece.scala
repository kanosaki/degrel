package degrel.dgspec.specs

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec.{NextPiece, SpecContext, SpecFactory, SpecPiece}

class ScriptRunSpecPiece(path: String) extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    NextPiece.Continue
  }
}

object ScriptRunSpecPiece {
  def decode(node: JsonNode)
            (implicit specFactory: SpecFactory): SpecPiece = {
    require(node.isTextual)
    new ScriptRunSpecPiece(node.asText())
  }
}
