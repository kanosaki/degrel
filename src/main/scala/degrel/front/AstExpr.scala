package degrel.front

import degrel.core.{Rule, Cell, Edge, Vertex}

/**
 * a -> b -> cのような，項と2項演算子の連続．演算子優先順位は別途定義されるため
 * それに従って木を生成します
 * @param first 最初の項(a)
 * @param following 続く(演算子,項)のSeq(-> b -> c)
 */
case class AstExpr(first: AstGraph, following: Seq[(AstBinOp, AstGraph)])
                  (implicit parserCtx: ParserContext) extends AstGraph with AstCellItem {

  override def toGraph(context: LexicalContext): Vertex = {
    if (following.isEmpty)
      return first.toGraph(context)
    val termList = buildTermList(null, Leaf(first), following.toList)
    val sortedTerm = termList.sorted
    sortedTerm.foreach(_.pullup())
    sortedTerm.last.builtNode.toGraph(parserCtx, context)
  }

  private def buildTermList(prev: BOp, prevTerm: Term, opsList: List[(AstBinOp, AstGraph)]): List[BOp] = {
    opsList match {
      case Nil => throw new RuntimeException("opsList should not to be Nil")
      case (op, graph) :: Nil => List(new BOp(prev, prevTerm, op, Leaf(graph), null))
      case (op, graph) :: tail => {
        val newterm = Leaf(graph)
        val newop = new BOp(prev, prevTerm, op, newterm, null)
        if (prev != null) {
          prev.nextOp = newop
        }
        newop :: buildTermList(newop, newterm, tail)
      }
    }
  }

  trait Term {
    def toGraph(pCtx: ParserContext, lCtx: LexicalContext): Vertex
  }

  /**
   * 演算木を生成するために演算子と項を線形に保持するクラス
   * {@example
   * a -> b * c + d
   * * の左の項はb, 前の演算子は->です
   * }
   * @param prevOp 前の演算子(左の項の左の演算子)
   * @param leftTerm 左の項
   * @param op この項の演算子
   * @param rightTerm 右の項
   * @param nextOp 次の演算子(右の項の右の演算子)
   */
  class BOp(var prevOp: BOp,
            var leftTerm: Term,
            val op: AstBinOp,
            var rightTerm: Term,
            var nextOp: BOp) extends Comparable[BOp] {

    var builtNode: Node = null

    def pullup() = {
      val node = new Node(op, leftTerm, rightTerm)
      if (prevOp != null) {
        prevOp.nextOp = this.nextOp
        prevOp.rightTerm = node
      }
      if (nextOp != null) {
        nextOp.prevOp = this.prevOp
        nextOp.leftTerm = node
      }
      builtNode = node
    }

    def isRoot: Boolean = {
      this.prevOp == null && this.nextOp == null
    }

    override def compareTo(o: BOp): Int = {
      this.op.compareTo(o.op)
    }
  }

  case class Node(op: AstBinOp,
                  left: Term,
                  right: Term) extends Term {
    /**
     * TODO: 外部に出したほうがいい
     */
    override def toGraph(pCtx: ParserContext, lCtx: LexicalContext): Vertex = {
      op match {
        case AstBinOp(SpecialLabel.Vertex.rule) =>
          Rule(left.toGraph(pCtx, lCtx), right.toGraph(pCtx, lCtx))
        case AstBinOp(other) =>
          Vertex.create(op.expr) { v =>
            Seq(
              Edge(v, SpecialLabel.Edge.lhs, left.toGraph(pCtx, lCtx)),
              Edge(v, SpecialLabel.Edge.rhs, right.toGraph(pCtx, lCtx))
            )
          }
      }
    }
  }

  case class Leaf(body: AstGraph) extends Term {
    override def toGraph(pCtx: ParserContext, lCtx: LexicalContext): Vertex = body.toGraph(lCtx)
  }
}

