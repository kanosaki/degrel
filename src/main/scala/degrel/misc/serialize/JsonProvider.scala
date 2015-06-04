package degrel.misc.serialize

import degrel.DegrelException
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods

/**
 * FormatProvider for JSON-like (JSON, yaml, ...) objects
 */
class JsonProvider extends FormatProvider[DDocument, JObject] {
  override def dump(in: DDocument): JObject = {
    "vertices" ->
      in.vertices.map(toJObj)
  }

  def toJObj(in: DVertex): JObject = {
    ("type" -> "vertex") ~
      ("id" -> in.id.toString) ~
      ("label" -> in.label) ~
      ("edges" -> in.edges.map {
        case DEdge(_, lbl, DRef(id)) =>
          ("type" -> "edge") ~
            ("label" -> lbl) ~
            ("ref" -> id.toString)
        case DEdge(_, lbl, dv: DVertex) =>
          ("type" -> "edge") ~
            ("label" -> lbl) ~
            ("dst" -> toJObj(dv))
      })
  }

  override def load(in: JObject): DDocument = {
    ???
  }

  override def dumpString(in: DDocument): String = {
    val json = this.dump(in)
    JsonMethods.pretty(JsonMethods.render(json))
  }

  override def loadString(in: String): DDocument = {
    JsonMethods.parse(in) match {
      case obj: JObject => this.load(obj)
      case _ => throw DegrelException("Invalid format")
    }
  }
}
