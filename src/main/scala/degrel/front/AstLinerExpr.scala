package degrel.front

/**
 * a -> b -> cのような，項と2項演算子の連続．演算子優先順位は別途定義されるため
 * それに従って木を生成します
 * @param first 最初の項(a)
 * @param following 続く(演算子,項)のSeq(-> b -> c)
 */
case class AstLinerExpr(first: AstVertex, following: Seq[(AstBinOp, AstVertex)])
                       (implicit parserCtx: ParserContext) extends AstNode with AstCellItem {

  def toTree: AstVertex = {
    // 単項の場合は，最初の項にデリゲート
    if (following.isEmpty)
      return first
    val termList = buildTermList(null, Leaf(first), following.toList)
    val splittedTerms = degrel.utils.collection.split(termList.sorted)(_.op == _.op)
    val sortedTerms = splittedTerms.flatMap { sameOps =>
      val assoc = sameOps(0).op.op.associativity
      assoc match {
        case OpAssoc.Left => sameOps
        case OpAssoc.Right => sameOps.reverse
      }
    }
    sortedTerms.foreach(_.pullup())
    val rootnode = sortedTerms.last.builtNode
    rootnode.toTree
  }

  /**
   * (項0, [(演算子1, 項1), (演算子2, 項2), ...])という形式から，BOpの双方向線形リストを生成し返します
   * @param prev 一つ前のBOp, 項0の場合はnull
   * @param prevTerm 一つ前の項．一番最初は項0を入れる
   * @param opsList 演算子2以降の(演算子,項)のリスト． 再帰で呼び出されているときは未処理の部分
   * @return BOpの双方向線形リスト
   */
  private def buildTermList(prev: BOp, prevTerm: Term, opsList: List[(AstBinOp, AstVertex)]): List[BOp] = {
    opsList match {
      case Nil => throw new RuntimeException("opsList should not to be Nil")
      case (op, graph) :: Nil => {
        val newop = new BOp(prev, prevTerm, op, Leaf(graph), null)
        if (prev != null) {
          prev.nextOp = newop
        }
        List(newop)
      }
      case (op, graph) :: tail => {
        val newterm = Leaf(graph)
        val newop = new BOp(prev, prevTerm, op, newterm, null)
        if (prev != null) {
          prev.nextOp = newop
        }
        // 出来たBOpをスタックに積んでリストを再帰的に構築します
        newop :: buildTermList(newop, newterm, tail)
      }
    }
  }

  trait Term {
    def toTree: AstVertex
  }

  /**
   * 演算木を生成するために演算子と項を線形に保持するクラス．
   * 二項演算子を評価することとは，抽象構文木において左右の子ノードを計算し，
   * 自身をその結果で置き換えることです．それを逆に行うことで抽象構文木を構築します
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

    /**
     * 現在持っている左項と右項，そして自分の演算子でNodeを生成します
     * それと同時に左の演算子の"次の演算子"を自分の次の演算子にセットし
     * 同様に右の演算子の"前の演算子"を自分の前の演算子にセットします．
     * すなわち，BOpの双方向線形リストの中から自分を消去します
     */
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

    /**
     * pullupを適切な順序で呼んだとき，式の一番根となる演算子(=最後に計算されるべき演算子)
     * であるかどうかを返します
     */
    def isRoot: Boolean = {
      this.prevOp == null && this.nextOp == null
    }


    override def equals(other: Any) = other match {
      case o: BOp => this.op == o.op
    }

    /**
     * 演算子優先順位のみに従って順序を定義します．
     * {@note 優先順位のみ == 結合方向は無視する}
     */
    override def compareTo(o: BOp): Int = {
      this.op.compareTo(o.op)
    }
  }

  case class Node(op: AstBinOp,
                  left: Term,
                  right: Term) extends Term {

    def toTree: AstVertex = {
      AstBinExpr(left.toTree, op, right.toTree)
    }
  }

  case class Leaf(body: AstVertex) extends Term {
    override def toTree: AstVertex = body
  }

}

