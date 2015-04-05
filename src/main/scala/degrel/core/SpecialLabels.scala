package degrel.core

/**
 * 特殊ラベルを定義します．"V_"は頂点に用いられるラベルで，”E_”は接続に用いられる特別ラベルです
 */
object SpecialLabels {
  /**
   * ワイルドカード(パターンマッチにおいて，何にでもマッチするパターン)
   * n.b. 以前は'*'でしたが，演算子使用可能文字で'*'を使いたいので'_'ヘ変更
   */
  val V_WILDCARD = '_

  /**
   * Cellの根のラベル
   */
  val V_CELL = '__cell__

  /**
   * 参照頂点
   */
  val V_REFERENCE = '__ref__

  /**
   * 書き換え規則頂点
   * NOTE: 二項演算子は演算子自体が頂点ラベルになるのが原則です．
   * 特殊頂点は__label__というアンダースコアに囲まれた形式ですが，
   * ここでは二項演算子に関するルールが優先されています
   */
  val V_RULE = '->

  /**
   * 参照頂点の参照先への接続
   */
  val E_REFERENCE_TARGET = '__to__

  /**
   * OthersEdgeを使用する際の対象となる頂点への接続
   */
  val E_OTHERS_EDGE = '__others__

  /**
   * 二項演算右辺への接続
   */
  val E_RHS = '__rhs__

  /**
   * 二項演算左辺への接続
   */
  val E_LHS = '__lhs__

  /**
   * Cellにおいて，
   */
  val E_CELL_RULE = '__rule__
  val E_CELL_ITEM = '__item__
  /**
   * Cellにおいて実行されるルールは，自分のCell内の`__rule__`頂点と
   * `__base__`で辿れるCellの`__rule__`頂点です
   */
  val E_CELL_BASE = '__base__
  val E_CONTINUE = '__continue__
  val E_VALUE = '__val__

  /**
   * パターングラフにおいて，その頂点が変数束縛対象の場合，何という名前で束縛されていたかを保存します
   */
  val A_CAPTURED_AS = '__captured_as__


  // 特殊名前空間名
  /**
   * 実行ルートCell
   */
  val N_MAIN = '__main__
}
