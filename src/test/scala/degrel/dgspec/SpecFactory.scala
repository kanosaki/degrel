package degrel.dgspec

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec.specs._

class SpecFactory {
  // Json Object
  def getObject(key: String, content: JsonNode): SpecPiece = {
    implicit val factory = this
    key match {
      case "seq_spec" => SequentialSpecPiece.decode(content)
      case "init" => SetValueSpecPiece.decode(content)
      case "assert" => AssertSpecPiece.decode(content)
      case other => throw new Exception(s"Unsupported type: $other")
    }
  }

  // String, Int, other primitive types.
  def getValue(v: Any): SpecPiece =  v match {
    case "rewrite" => new RewriteSpecPiece()
  }
}
