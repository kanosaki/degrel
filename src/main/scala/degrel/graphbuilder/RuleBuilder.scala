package degrel.graphbuilder

import degrel.core.{Label, Edge, Vertex, Rule}
import degrel.front.{BinOp, AstBinExpr}

class RuleBuilder(val parent: Primitive, ast: AstBinExpr) extends Builder[Rule] {
  assert(ast.op == BinOp.RULE)

  val lhsFactory = factory.get[Vertex](this, ast.left)
  val rhsFactory = factory.get[Vertex](this, ast.right)

  override val children = Seq(lhsFactory, rhsFactory)

  /**
   * このグラフ要素への参照用のヘッダ
   */
  override val header: Rule = Rule(lhsFactory.header, rhsFactory.header)

  /**
   * このグラフ要素における環境
   */
  override def variables: LexicalVariables = ???

  /**
   * このグラフ要素を直接内包するCell
   */
  override def outerCell: CellBuilder = parent.outerCell

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   */
  override def concrete(): Unit = {}
}
