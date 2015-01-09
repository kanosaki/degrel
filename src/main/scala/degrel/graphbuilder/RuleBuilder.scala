package degrel.graphbuilder

import degrel.core.{Rule, Vertex}
import degrel.front.{AstBinExpr, BinOp}

class RuleBuilder(val parent: Primitive, ast: AstBinExpr) extends Builder[Rule] {
  assert(ast.op == BinOp.RULE)

  /**
   * このグラフ要素における環境
   */
  override val variables: LexicalVariables = new RuleVariables(parent.variables)

  val lhsFactory = factory.get[Vertex](this, ast.left)
  val rhsFactory = factory.get[Vertex](this, ast.right)

  override val children = Seq(lhsFactory, rhsFactory)

  /**
   * このグラフ要素への参照用のヘッダ
   */
  override val header: Rule = Rule(lhsFactory.header, rhsFactory.header)


  /**
   * このグラフ要素を直接内包するCell
   */
  override def outerCell: CellBuilder = parent.outerCell

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   */
  override def concrete(): Unit = {}

  class RuleVariables(val parent: LexicalVariables) extends LexicalVariables {
  }
}

