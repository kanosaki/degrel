package degrel.dgspec.specs

import java.io.File

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec._
import org.apache.commons.io.FilenameUtils

class ScriptRunSpecPiece(path: String) extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    val f = new File(FilenameUtils.concat("dgspec", path))
    val interpreter = new SpecInterpreter(f)
    interpreter.start()
    ctx.lastOutput = interpreter.lastOutput
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
