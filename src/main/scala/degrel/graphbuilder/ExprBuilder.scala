package degrel.graphbuilder

import degrel.core.Vertex
import degrel.front.AstBinExpr

class ExprBuilder(val parent: Primitive, val ast: AstBinExpr) extends Builder[Vertex] {
  /**
   * このグラフ要素における環境
   */
  override def variables: LexicalVariables = ???

  override def children: Iterable[Primitive] = ???

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   */
  override def concrete(): Unit = ???

  /**
   * このグラフ要素を直接内包するCell
   */
  override def outerCell: CellBuilder = ???

  /**
   * このグラフ要素への参照用のヘッダ
   */
  override def header: Vertex = ???
}
