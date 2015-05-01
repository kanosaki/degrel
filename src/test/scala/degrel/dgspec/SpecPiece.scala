package degrel.dgspec

import org.scalatest.FlatSpecLike

trait SpecPiece extends FlatSpecLike {

  /**
   * 指定された`SpecContext`でこの`SpecPiece`を実行します
   */
  def evaluate(ctx: SpecContext): NextPiece

}

/**
 * SpecPieceが終了した際に，次にどうするべきかを指定します
 */
trait NextPiece

object NextPiece {

  /**
   * `SpecPiece`は正常に終了したため，デフォルトの次の`SpecPiece`へ
   * 進むことを指定します
   */
  case object Continue extends NextPiece

  /**
   * `SpecPiece`が失敗したため，このspecの実行の停止を要求します
   */
  case class Abort(msg: String) extends NextPiece

}

