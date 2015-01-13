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

/**
 * 接続のラベルは省略可能です．接続可能な接続は`AstAbbrEdge`として表現され，`toFullForm`を呼び出すことで
 * 通常の`AstEdge`へ変換できます．
 * 現在の使用では，省略した場合はその接続の場所に応じて番号で接続が与えられます．つまり
 * `foo(bar, baz, hoge, x: piyo)`
 * は，以下のように展開されます
 * `foo(0: bar, 1: baz, 2: hoge, x: piyo`
 */
case class AstAbbrEdge(label: Option[AstLabel], dst: AstFunctor) extends AstNode {
  def toFullForm(pos: Int): AstEdge = {
    label match {
      case Some(lbl) => AstEdge(lbl, dst)
      case None => AstEdge(AstLabel(pos.toString), dst)
    }
  }
}

