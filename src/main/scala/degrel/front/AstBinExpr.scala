package degrel.front

import degrel.core.Vertex

case class AstBinExpr(left: AstExpr[Vertex],
                      astop: AstBinOp,
                      right: AstExpr[Vertex])
  extends AstExpr[Vertex] with AstCellItem {
  val op = astop.op

}
