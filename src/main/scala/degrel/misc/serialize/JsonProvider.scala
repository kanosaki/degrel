package degrel.misc.serialize

import degrel.core.Vertex
import org.json4s._

/**
 * FormatProvider for JSON-like (JSON, yaml, ...) objects
 */
class JsonProvider extends FormatProvider[Vertex, String] {
  override def dump(in: Vertex): String = {
    ???
  }

  override def load(in: String): Vertex = {
    ???
  }

  protected def dumpVertex(v: Vertex): JObject = {
    import JsonDSL._
    import native.JsonMethods._
    ???
  }
}
