package degrel.front.dotlike

case class AstDigraph(label: String, body: AstDigraphBody) {

}

trait AstDigraphElement {

}

case class AstDigraphEdge(fromLabel: String, toLabel: String, edgeLabel: String) extends AstDigraphElement {

}

case class AstDigraphAttributes(vertexLabel: String, attributes: Seq[(String, String)]) extends AstDigraphElement {

}

case object AstDigraphEmptyLine extends AstDigraphElement {

}

case class AstDigraphBody(elements: Iterable[AstDigraphElement]) {

}

