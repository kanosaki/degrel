package degrel.dgspec.specs

import java.io.File

import com.fasterxml.jackson.databind.JsonNode
import degrel.control.{BootArguments, Bootstrapper}
import scala.collection.JavaConversions._
import degrel.dgspec._
import org.apache.commons.io.FilenameUtils

class ScriptRunSpecPiece(bootsrapper: Bootstrapper) extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    val interpreter = new SpecInterpreter(bootsrapper.createChassis())
    interpreter.start()
    ctx.lastOutput = interpreter.lastOutput
    NextPiece.Continue
  }
}

object ScriptRunSpecPiece {
  def decode(node: JsonNode)
            (implicit specFactory: SpecFactory): SpecPiece = {
    val bootstrapper = if (node.isTextual) {
      // Argumentがtextのみのときは，ファイル名として解釈します
      val filename = FilenameUtils.concat("dgspec", node.asText())
      BootArguments(
        script = Some(new File(filename))
      ).createBootstrapper()
    } else if (node.isObject) {
      // ArgumentがObjectの時は，fileフィールドからファイル名を
      // optionsからインタプリタオプションを受け取ります
      val filenameObj = node.get("file")
      require(filenameObj.isTextual)
      val filename = FilenameUtils.concat("dgspec", filenameObj.asText())
      val options = node.get("options")
        .fields()
        .toIterator
        .map(kv => kv.getKey -> kv.getValue.asText())
        .toMap
      BootArguments(
        script = Some(new File(filename)),
        options = options
      ).createBootstrapper()
    } else {
      throw new RuntimeException("Unsupported spec format!")
    }
    new ScriptRunSpecPiece(bootstrapper)
  }
}
