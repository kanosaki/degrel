package degrel.dgspec

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import degrel.dgspec.specs.VoidSpecPiece

import scala.collection.JavaConversions._

case class SpecFile(caption: String,
                    spec: SpecPiece,
                    description: Option[String]) extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    spec.evaluate(ctx)
  }
}

object SpecFile {
  val ROOT_SPEC_NAMES = Set("caption", "description", "version")
  val SPEC_VERSION = "1"

  // TODO: DI使ったほうがいい?
  def defaultMapper: ObjectMapper = {
    val mapper = new ObjectMapper(new YAMLFactory())
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  def getRequired(node: JsonNode, key: String): JsonNode = {
    val value = node.get(key)
    if (value == null) {
      throw new Exception(s"$key required")
    }
    value
  }

  def getOption(node: JsonNode, key: String): Option[JsonNode] = {
    val value = node.get(key)
    if (value != null) {
      Some(value)
    } else {
      None
    }
  }

  def decode(node: JsonNode)(implicit specFactory: SpecFactory): SpecFile = {
    val caption = getRequired(node, "caption").asText
    val description = getOption(node, "description").flatMap(n => Some(n.asText()))
    val version = getRequired(node, "version").asInt.toString
    require(version == SPEC_VERSION, s"Incompatible spec file(version $version), supported version: $SPEC_VERSION")
    val otherFields = node
      .fields
      .filter(entry => !ROOT_SPEC_NAMES.contains(entry.getKey))
    val specs = otherFields
      .map(entry => specFactory.getObject(entry.getKey, entry.getValue))
      .toList
    specs match {
      case Nil => new SpecFile(caption, VoidSpecPiece, description)
      case first :: Nil => new SpecFile(caption, specs.head, description)
      case _ => throw new Exception("Cannot handle 2 or more root specs.")
    }
  }
}