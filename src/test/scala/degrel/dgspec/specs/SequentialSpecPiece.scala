package degrel.dgspec.specs

import com.fasterxml.jackson.databind.JsonNode
import degrel.dgspec.{NextPiece, SpecContext, SpecFactory, SpecPiece}
import org.scalatest.exceptions.TestFailedException

import scala.collection.JavaConversions._

case class SequentialSpecPiece(children: List[SpecPiece]) extends SpecPiece {

  override def evaluate(ctx: SpecContext): NextPiece = {
    evalSpecs(ctx, children)
  }

  def tryEvalSpec(ctx: SpecContext, specPiece: SpecPiece): NextPiece = {
    try {
      specPiece.evaluate(ctx)
    } catch {
      case th: Throwable => {
        fail(s"Error during executing: $specPiece", th)
      }
    }
  }

  protected def evalSpecs(ctx: SpecContext, remainSpecs: List[SpecPiece]): NextPiece = {
    remainSpecs match {
      case hd :: tl => {
        tryEvalSpec(ctx, hd) match {
          case NextPiece.Continue => evalSpecs(ctx, tl)
          case other => other
        }
      }
      case Nil => NextPiece.Continue
    }
  }
}

object SequentialSpecPiece {
  def decode(node: JsonNode)
            (implicit specFactory: SpecFactory): SequentialSpecPiece = {
    require(node.isArray, "Sequential spec must be an array.")
    val items = node.elements().map(elem => {
      if (elem.isObject) {
        val first = elem.fields.toSeq.headOption
        first match {
          case Some(spec) => specFactory.getObject(spec.getKey, spec.getValue)
          case None => throw new Exception(s"At least one spec required at $elem")
        }
      } else if (elem.isTextual) {
        specFactory.getValue(elem.asText)
      } else {
        // TODO: Change exception type
        throw new Exception(s"Unsupported spec $elem")
      }
    })
    new SequentialSpecPiece(items.toList)
  }
}
