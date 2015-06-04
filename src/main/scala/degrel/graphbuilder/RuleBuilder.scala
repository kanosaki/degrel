package degrel.graphbuilder

import degrel.core._
import degrel.front.{AstBinExpr, BinOp}

class RuleBuilder(val parent: Primitive, ast: AstBinExpr) extends Builder[Rule] {
  assert(ast.op == BinOp.RULE)

  /**
   * このグラフ要素における環境
   */
  override val variables: LexicalSymbolTable = parent.variables

  val lhsScope = new Scope(this)
  val rhsScope = new Scope(lhsScope)
  val lhsFactory = factory.get[Vertex](lhsScope, ast.left)
  val rhsFactory = factory.get[Vertex](rhsScope, ast.right)

  override val children = Seq(lhsFactory, rhsFactory)

  /**
   * このグラフ要素への参照用のヘッダ
   */
  override val header: Rule = new RuleVertexHeader(null, null)

  /**
   * このグラフ要素を直接内包するCell
   */
  override def outerCell: CellBuilder = parent.outerCell

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   */
  override def doBuildPhase(phase: BuildPhase): Unit = phase match {
    case FinalizePhase => {
      val h = this.header.asInstanceOf[RuleVertexHeader]
      h.write(new RuleVertexBody(lhsFactory.header, rhsFactory.header))
    }
    case _ =>
  }
}

