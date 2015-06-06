package degrel.front.graphbuilder

import degrel.core._
import degrel.front.AstBinExpr

/**
 * 二項演算子含む式を表現するグラフを構築します
 */
class ExprBuilder(val parent: Primitive, val ast: AstBinExpr) extends Builder[Vertex] {

  /**
   * 左辺の{@code Builder[T]}
   */
  val lhsFactory = factory.get[Vertex](this, ast.left)

  /**
   * 右辺の{@code Builder[T]}
   */
  val rhsFactory = factory.get[Vertex](this, ast.right)

  /**
   * @inheritdoc
   */
  override val children = Seq(lhsFactory, rhsFactory)

  /**
   * このグラフ要素への参照用のヘッダ
   */
  override val header: Vertex = VertexHeader(null)

  /**
   * このグラフ要素における環境
   */
  override def variables: LexicalSymbolTable = parent.variables

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   */
  override def doBuildPhase(phase: BuildPhase): Unit = phase match {
    case FinalizePhase => {
      val h = this.header.asInstanceOf[VertexHeader]
      h.write(Vertex.create(ast.op.expr)(h => {
        Seq(
          Edge(h, Label.E.lhs, lhsFactory.header),
          Edge(h, Label.E.rhs, rhsFactory.header)
        )
      }))
    }
    case _ =>
  }

  /**
   * このグラフ要素を直接内包するCell
   */
  override def outerCell: CellBuilder = parent.outerCell
}
