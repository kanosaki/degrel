package degrel.front

import degrel.core

class Ast(val root: AstNode) {

}

trait AstNode {
}

case class AstGraph(roots: Seq[AstRoot]) extends AstNode {

}

trait AstRoot extends AstNode {
  def toGraph(context: LexicalContext): core.Vertex
}

case class AstRule(lhs: AstRoot, rhs: AstRoot) extends AstRoot {
  def toGraph(context: LexicalContext): core.Vertex = {
    throw new NotImplementedError()
  }
}

case class AstVertex(name: AstName, edges: Seq[AstEdge]) extends AstRoot {
  def toGraph(context: LexicalContext): core.Vertex = {
    throw new NotImplementedError()
  }

  def capture: List[(String, AstVertex)] = {
    name match {
      case AstName(Some(AstCapture(e)), _) => (e, this) :: this.captureEdges
      case _ => this.captureEdges
    }
  }

  private def captureEdges: List[(String, AstVertex)] = {
    this.edges.map(_.capture).flatten.toList
  }
}

case class AstEdge(label: AstLabel, dst: AstVertex) extends AstNode {
  def capture = dst.capture
}

trait AstLiteral extends AstNode {

}

case class AstLabel(expr: String) extends AstLiteral {

}

case class AstCapture(expr: String) extends AstLiteral {

}

case class AstName(capture: Option[AstCapture], label: Option[AstLabel]) {

}
