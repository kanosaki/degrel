package degrel.front.dotlike

import degrel.front.LexicalContext

case class AstDigraph(label: String, body: AstDigraphBody) {

  def toGraph(context: LexicalContext = LexicalContext.empty) = {
    val builder = new DotlikeBuilder(this)(context)
    builder.root
  }
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

