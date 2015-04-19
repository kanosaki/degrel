package degrel.front

import degrel.core.Vertex

/**
 * 演算子の方向，順序に従って木として構成された二項演算子による式を表します．
 * @param left
 * @param astop
 * @param right
 */
case class AstBinExpr(left: AstExpr[Vertex],
                      astop: AstBinOp,
                      right: AstExpr[Vertex])
  extends AstExpr[Vertex] with AstCellItem {
  val op = astop.op

}
