package degrel.front

import degrel.core

class Ast(val root: AstNode) {

}

trait AstNode {

}

class CodeException(msg: String) extends Exception {

}

case class AstGraph(roots: Seq[AstRoot]) extends AstNode {

}

trait AstRoot extends AstNode {
  def toGraph(context: LexicalContext): core.Vertex
}

case class AstRule(lhs: AstRoot, rhs: AstRoot) extends AstRoot {
  def toGraph(context: LexicalContext): core.Vertex = {
    // Capture lhs variables
    val lhsCapture = lhs match {
      case v: AstVertex => v.capture(context)
      case _ => throw new CodeException("A rule only can take vertex on its left hand.")
    }
    val rhsContext = new RhsContext(parent = context)(lhsCapture)
    val lhsContext = new LhsContext(parent = context)
    core.Vertex(
      label = BinOp.rule,
      edges = Map(
        SpecialLabel.Edge.lhs -> lhs.toGraph(lhsContext),
        SpecialLabel.Edge.rhs -> rhs.toGraph(rhsContext)
      )
    )
  }
}

case class AstVertex(name: AstName, edges: Seq[AstEdge]) extends AstRoot {
  def toGraph(context: LexicalContext): core.Vertex = {
    (name, context.isPattern) match {
      case (AstName(Some(AstCapture(cap)), _), false) =>
        core.Vertex(
          label = SpecialLabel.Vertex.reference,
          edges = Map(
            SpecialLabel.Edge.ref -> context.resolveExact[core.Vertex](cap)
          )
        )
      case _ => {
        core.Vertex(
          label = this.labelExpr,
          edges = edges.map(_.toEdge(context)).toMap
        )
      }
    }
  }

  def labelExpr: String = name match {
    case AstName(_, Some(AstLabel(l))) => l
    case AstName(_, None) => SpecialLabel.Vertex.wildcard
  }

  def capture(context: LexicalContext): List[(String, core.Vertex)] = {
    name match {
      case AstName(Some(AstCapture(e)), _) => (e, this.toGraph(context)) :: this.captureEdges(context)
      case _ => this.captureEdges(context)
    }
  }

  private def captureEdges(context: LexicalContext): List[(String, core.Vertex)] = {
    this.edges.map(_.capture(context)).flatten.toList
  }
}

case class AstEdge(label: AstLabel, dst: AstVertex) extends AstNode {
  def capture(context: LexicalContext) = dst.capture(context)
  def toEdge(context: LexicalContext) : (String, core.Vertex) = {
    (label.expr, dst.toGraph(context))
  }
}

trait AstLiteral extends AstNode {

}

case class AstLabel(expr: String) extends AstLiteral {

}

case class AstCapture(expr: String) extends AstLiteral {

}

case class AstName(capture: Option[AstCapture], label: Option[AstLabel]) {

}
