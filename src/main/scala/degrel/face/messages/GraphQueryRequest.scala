package degrel.face.messages

case class GraphQueryRequest(id: Option[Int], label: Option[String]) {
  require(id.isDefined || label.isDefined, "id or label required")
}
