package degrel.dgspec.specs

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec.{NextPiece, SpecContext, SpecFactory, SpecPiece}

case class ProgramSpecPiece(program: String, namespace: Option[String]) extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    ???
    NextPiece.Continue
  }
}

object ProgramSpecPiece {
  def decode(node: JsonNode)
            (implicit specFactory: SpecFactory): ProgramSpecPiece = {
    if (node.isTextual) {
      new ProgramSpecPiece(node.asText, None)
    } else if (node.isObject) {
      val codeNode = node.get("code")
      val namespaceNode = node.get("namespace")
      if (codeNode == null) {
        throw new RuntimeException("code required")
      }
      val ns = if (namespaceNode == null) None else Some(namespaceNode.asText())
      new ProgramSpecPiece(codeNode.asText(), ns)
    } else {
      throw new RuntimeException("invalid program piece")
    }
  }
}

