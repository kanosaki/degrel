package degrel.dgspec

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import degrel.dgspec.specs.VoidSpecPiece

import scala.collection.JavaConversions._

case class SpecFile(description: String,
                    spec: SpecPiece) extends SpecPiece {
  override def evaluate(ctx: SpecContext): NextPiece = {
    spec.evaluate(ctx)
  }
}

object SpecFile {
  val ROOT_SPEC_NAMES = Set("description", "version")
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

  def decode(node: JsonNode)(implicit specFactory: SpecFactory): SpecFile = {
    val description = getRequired(node, "description").asText
    val version = getRequired(node, "version").asInt.toString
    require(version == SPEC_VERSION, s"Incompatible spec file(version $version), supported version: $SPEC_VERSION")
    val otherFields = node
      .fields
      .filter(entry => !ROOT_SPEC_NAMES.contains(entry.getKey))
    val specs = otherFields
      .map(entry => specFactory.getObject(entry.getKey, entry.getValue))
      .toList
    specs match {
      case Nil => new SpecFile(description, VoidSpecPiece)
      case first :: Nil => new SpecFile(description, specs.head)
      case _ => throw new Exception("Cannot handle 2 or more root specs.")
    }
  }
}