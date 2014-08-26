package degrel.visualize.viewmodel.grapharranger

import degrel.core.Vertex

/**
 * グラフ描画アルゴリズムの実装．主にグラフの頂点の位置を調整します
 */
trait GraphArranger {
  /**
   * 頂点を追加
   * @param v
   */
  def pushVertex(v: Vertex): Unit

  /**
   * 頂点を固定
   */
  def stickVertex(v: Vertex): Unit

  /**
   * データをクリアします
   */
  def clear(): Unit

  /**
   * 位置調整処理を1段階行います
   */
  def tick(): Unit

  /**
   * 頂点のリスト
   */
  def vertices: Iterable[ArrangerVertexInfo]

  /**
   * 接続のリスト
   */
  def edges: Iterable[ArrangerEdgeInfo]

  /**
   * 調整が完了したかどうかを表します
   */
  def isCompleted: Boolean
}
