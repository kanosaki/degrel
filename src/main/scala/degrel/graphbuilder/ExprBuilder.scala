package degrel.graphbuilder

import degrel.core.{Edge, Label, Vertex}
import degrel.front.AstBinExpr

class ExprBuilder(val parent: Primitive, val ast: AstBinExpr) extends Builder[Vertex] {

  val lhsFactory = factory.get[Vertex](this, ast.left)
  val rhsFactory = factory.get[Vertex](this, ast.right)

  override val children = Seq(lhsFactory, rhsFactory)

  /**
   * このグラフ要素への参照用のヘッダ
   */
  override val header: Vertex = Vertex.create(ast.op.expr)(h => {
    Seq(
      Edge(h, Label.lhs, lhsFactory.header),
      Edge(h, Label.rhs, rhsFactory.header)
    )
  })

  /**
   * このグラフ要素における環境
   */
  override def variables: LexicalVariables = parent.variables

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   */
  override def concrete(): Unit = {}

  /**
   * このグラフ要素を直接内包するCell
   */
  override def outerCell: CellBuilder = parent.outerCell
}
