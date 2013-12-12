package degrel.front.dotlike

case class AstDigraph(label: String, body: AstDigraphBody) {

}

trait AstDigraphElement {

}

case class AstDigraphEdge(fromLabel: String, toLabel: String, edgeLabel: String) extends AstDigraphElement {

}

case class AstDigraphAttributes(vertexLabel: String, attributes: Map[String, String]) extends AstDigraphElement {

}

case class AstDigraphBody(elements: Iterable[AstDigraphElement]) {

}


case class AstDiEdgePiece(dstVertex: String, edgeLabel: String) {

}

case class AstDiAttrPiece(attributes: Map[String, String]) {

}
