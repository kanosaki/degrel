package degrel.front

import degrel.core
import degrel.core.Rule
import degrel.utils.FlyWrite._

/**
 * 抽象構文木のコンテナクラス
 */
class Ast(val root: AstNode) {
  def toGraph(context: LexicalContext = LexicalContext.empty) = {
    root match {
      case rt: AstRoot => rt.toGraph(context)
      case gr: AstGraph => gr.roots.head.toGraph(context)
      case _ => throw new RuntimeException("This ast is not represents a graph")
    }
  }
}

/**
 * すべての抽象構文木要素の親となる型
 */
trait AstNode {

}

/**
 * プログラム上の制約違反
 * @param msg
 */
class CodeException(msg: String) extends FrontException(msg) {

}

/**
 * グラフを表すAST
 */
case class AstGraph(roots: Seq[AstRoot]) extends AstNode {

}

/**
 * 根のAST
 */
trait AstRoot extends AstNode {
  /**
   * この抽象構文木からグラフを構成します
   * @param context 現在このグラフが存在するContext
   * @return 構成された頂点
   */
  def toGraph(context: LexicalContext): core.Vertex
}

/**
 * ルールのAST
 * @param lhs 左辺を表すグラフの根
 * @param rhs 右辺を表すグラフの根
 */
case class AstRule(lhs: AstRoot, rhs: AstRoot) extends AstRoot {
  /**
   * ルールを表すASTを構成します．ルールのグラフの構成手順は
   * 1. LhsContextを生成する
   * 2. LhsContextを元に左辺の変数を束縛しRhsContextを作成する
   * 3. 両方のContextでグラフを構成する
   * @param context 現在このグラフが存在するContext
   * @return 構成された頂点
   */
  def toGraph(context: LexicalContext): core.Vertex = {
    // Capture lhs variables
    val lhsContext = new LhsContext(parent = context)
    lhs match {
      case v: AstVertex => v.capture(lhsContext)
      case _ => throw new CodeException("A rule only can take vertex on its left hand.")
    }
    val lhsGraph = lhs.toGraph(lhsContext)
    val rhsContext = new RhsContext(parent = context)(lhsContext)
    val rhsGraph = rhs.toGraph(rhsContext)
    Rule(lhsGraph, rhsGraph)
  }
}


/**
 * 頂点を表すAST
 */
case class AstVertex(name: AstName, attributes: Option[Seq[AstAttribute]], edges: Seq[AstEdge]) extends AstRoot {

  /**
   * 頂点を表すVertexを作成します．参照頂点の場合はContextから探索し見つかったものを参照します
   * @param context 現在このグラフが存在するContext
   * @return 構成された頂点
   */
  def toGraph(context: LexicalContext): core.Vertex = {
    (this.captureExpr, context.isPattern) match {
      // Make reference vertex
      case (Some(cap), false) => this.mkReferenceVertex(cap, context)
      case (Some(cap), true) => this.mkLhsGraph(context.asInstanceOf[LhsContext])
      case _ => this.mkGraph(context)
    }
  }

  def mkReferenceVertex(cap: String, context: LexicalContext): core.Vertex = {
    // TODO: _ref 接続を持つ場合はエラー
    val label = SpecialLabel.Vertex.reference
    core.Vertex.create(label, this.mkAttributesMap) { v =>
      val refEdge = core.Edge(v, SpecialLabel.Edge.ref, context.resolveExact[core.Vertex](cap))
      Stream(refEdge) ++ this.edges.map(_.toEdge(context))
    }
  }

  def mkLhsGraph(lhsContext: LhsContext): core.Vertex = {
    lhsContext.fromCaptureCache(this) match {
      case Some(v) => v
      case None => {
        val graph = this.mkGraph(lhsContext)
        lhsContext.storeCaptureCache(this, graph)
        graph
      }
    }
  }

  def mkGraph(context: LexicalContext): core.Vertex = {
    val label = this.labelExpr
    val eds = edges.map(_.toEdge(context))
    core.Vertex(label, eds.toStream, this.mkAttributesMap)
  }

  def labelExpr: String = name match {
    case AstName(_, Some(AstLabel(l))) => l
    case AstName(_, None) => SpecialLabel.Vertex.wildcard
  }

  /**
   * 頂点が変数を持つ場合はその変数を返します，その際に構成されたGraphは
   * 後で参照の整合性を保つためにContextへキャッシュされます
   */
  def capture(context: LexicalContext): Unit = {
    this.captureEdges(context)
    this.captureExpr match {
      case Some(cap) => context match {
        case lhsContext: LhsContext => lhsContext.storeCaptureMap(cap, this)
      }
      case None =>
    }
  }

  def captureExpr: Option[String] = name match {
    case AstName(Some(AstCapture(cap)), _) => Some(cap)
    case _ => None
  }

  private def captureEdges(context: LexicalContext): Unit = {
    for (edge <- this.edges)
      edge.capture(context)
  }

  def mkAttributesMap: Map[String, String] = {
    val srcattrs = this.attributes match {
      case Some(attrs) => attrs.map(ast => (ast.key, ast.value))
      case None => Map()
    }
    (srcattrs ++ this.mkMetadataAttribtues).toMap
  }

  def mkMetadataAttribtues: Iterable[(String, String)] = {
    this.name match {
      case AstName(Some(AstCapture(e)), _) => Seq("__captured_as__" -> e)
      case _ => Seq()
    }
  }
}

case class AstEdge(label: AstLabel, dst: AstVertex) extends AstNode {
  def capture(context: LexicalContext): Unit = dst.capture(context)

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

case class AstAttribute(key: String, value: String) {

}