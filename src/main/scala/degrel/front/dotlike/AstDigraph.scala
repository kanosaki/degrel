package degrel.front.dotlike

import degrel.graphbuilder.LexicalSymbolTable

case class AstDigraph(label: String, body: AstDigraphBody) {

  def toGraph(context: LexicalSymbolTable = LexicalSymbolTable.empty) = {
    val builder = new DotlikeBuilder(this)(context)
    builder.root
  }
}

trait AstDigraphElement {

}

object AstDigraphElement {
  // DUMMY = "仮"
  val DUMMY_ROOT_LABEL = ""
  val IDENTIFIER_SEPARATOR = "$"
}

case class AstDigraphEdge(fromLabel: AstDigraphIdentifier, toLabel: AstDigraphIdentifier, edgeLabel: String)
  extends AstDigraphElement {

}

case class AstDigraphAttributes(vertexLabel: String, attributes: Seq[(String, String)])
  extends AstDigraphElement {

}

case object AstDigraphEmptyLine extends AstDigraphElement {

}

case class AstDigraphBody(elements: Iterable[AstDigraphElement]) {

}

case class AstDigraphIdentifier(label: Option[String], identifier: Option[String])
  extends AstDigraphElement {

  def toIdentifier: String = {
    identifier match {
      case None => optionToString(label)
      case Some(ident) => optionToString(label) ++ AstDigraphElement.IDENTIFIER_SEPARATOR ++ ident
    }
  }

  private def optionToString(v: Option[String]): String = {
    v match {
      case None => ""
      case Some(s) => s
    }
  }
}

