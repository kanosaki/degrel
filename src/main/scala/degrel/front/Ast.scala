package degrel.front

class Ast(val root: AstNode) {

}

trait AstNode {
}

case class AstGraph(roots: Seq[AstRoot]) extends AstNode {

}

trait AstRoot extends AstNode {

}

case class AstRule(lhs: AstRoot, rhs: AstRoot) extends AstRoot {

}

case class AstVertex(name: AstName, edges: Seq[AstEdge]) extends AstRoot {

}

case class AstEdge(name: AstName, dst: AstVertex) extends AstNode {

}

trait AstLiteral extends AstNode {

}

case class AstLabel(expr: String) extends AstLiteral {

}

case class AstCapture(expr: String) extends AstLiteral {

}

case class AstName(capture: Option[AstCapture], label: Option[AstLabel]) {

}
