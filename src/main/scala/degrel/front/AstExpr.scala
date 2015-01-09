package degrel.front

import degrel.core.Vertex

/**
 * グラフを生成し，かつDEGRELのプログラム側から式として扱える物を表します
 * AstGraphとの違いは，AstGraphなどはimportsなどプログラム制御用のグラフを生成します
 */
trait AstExpr[+T <: Vertex] extends AstGraph[T] with AstCellItem {

}

/**
 * 頂点を表すAST
 */
case class AstFunctor(name: AstName, attributes: Option[Seq[AstAttribute]], edges: Seq[AstEdge]) extends AstExpr[Vertex] {
  def labelExpr: String = name match {
    case AstName(Some(AstLabel(l)), _) => l
    case AstName(None, _) => SpecialLabel.Vertex.wildcard.name
  }

  def captureExpr: Option[String] = name match {
    case AstName(_, Some(AstVertexBinding(cap))) => Some(cap)
    case _ => None
  }
}

case class AstEdge(label: AstLabel, dst: AstFunctor) extends AstNode {
}

