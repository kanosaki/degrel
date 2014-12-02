package degrel.front

import degrel.core.Vertex

case class AstBinExpr(left: AstExpr[Vertex], op: AstBinOp, right: AstExpr[Vertex]) extends AstExpr[Vertex] with AstCellItem {

}
