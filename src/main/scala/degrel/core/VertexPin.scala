package degrel.core

import org.json4s.JsonAST.{JString, JValue}

case class VertexPin(id: ID, version: Long) {
  def toSimpleJson: JValue = {
    JString(id.toString)
  }
}
