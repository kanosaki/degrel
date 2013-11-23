package degrel.front

import degrel.core
import degrel.utils.FlyWrite._

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
    val lhsContext = new LhsContext(parent = context)
    val lhsCapture = lhs match {
      case v: AstVertex => v.capture(lhsContext)
      case _ => throw new CodeException("A rule only can take vertex on its left hand.")
    }
    val rhsContext = new RhsContext(parent = context)(lhsCapture)
    lhs.toGraph(lhsContext) |->| (rhs.toGraph(rhsContext))
  }
}

case class AstVertex(name: AstName, edges: Seq[AstEdge]) extends AstRoot {
  def toGraph(context: LexicalContext): core.Vertex = {
    (name, context.isPattern) match {
      // Make reference vertex
      case (AstName(Some(AstCapture(cap)), _), false) => this.mkReferenceVertex(cap, context)
      case _ => context match {
        // Make vertex
        case lhsContext: LhsContext => this.mkLhsGraph(lhsContext)
        case _ => this.mkGraph(context)
      }
    }
  }

  def mkReferenceVertex(cap: String, context: LexicalContext): core.Vertex = {
    SpecialLabel.Vertex.reference |^|
    (SpecialLabel.Edge.ref |:| context.resolveExact[core.Vertex](cap))
  }

  def mkLhsGraph(lhsContext: LhsContext): core.Vertex = {
    lhsContext.fromCaptureCache(this) match {
      case Some(v) => v
      case None => this.mkGraph(lhsContext)
    }
  }

  def mkGraph(context: LexicalContext): core.Vertex = {
    this.labelExpr |^| (edges.map(_.toEdge(context)))
  }

  def labelExpr: String = name match {
    case AstName(_, Some(AstLabel(l))) => l
    case AstName(_, None) => SpecialLabel.Vertex.wildcard
  }

  def capture(context: LexicalContext): List[(String, core.Vertex)] = {
    name match {
      case AstName(Some(AstCapture(e)), _) => context match {
        case lhsContext: LhsContext => {
          val graph = this.toGraph(lhsContext)
          lhsContext.storeCaptureCache(this, graph)
          (e, graph) :: this.captureEdges(lhsContext)
        }
        case _ => (e, this.toGraph(context)) :: this.captureEdges(context)
      }
      case _ => this.captureEdges(context)
    }
  }

  private def captureEdges(context: LexicalContext): List[(String, core.Vertex)] = {
    this.edges.map(_.capture(context)).flatten.toList
  }
}

case class AstEdge(label: AstLabel, dst: AstVertex) extends AstNode {
  def capture(context: LexicalContext) = dst.capture(context)

  def toEdge(context: LexicalContext): core.Edge = {
    label.expr |:| dst.toGraph(context)
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
