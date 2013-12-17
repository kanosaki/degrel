package degrel.rewriting

import degrel.core.{VertexHeader, Rule, Vertex}
import akka.actor.{Props, Actor}


/**
 * 書き換えの手続きを管理します
 * @todo トランザクションに対応
 */
class Rewriter(val rule: Rule)  {
  /**
   * targetに指定された頂点を根としてパターンマッチを行い，マッチすれば書き換えを行います
   * 書き換えを行った場合はtrueを，書き換えを行わなかった場合はfalseを返します
   * @param target
   * @return
   */
  def begin(target: Vertex): Boolean = {
    val mch = target.matches(rule.lhs)
    if (mch.success) {
      val binding = this.pick(mch.pack)
      val builtGraph = this.build(binding)
      target match {
        case vh: VertexHeader => vh.write(builtGraph)
        case _ => throw new IllegalArgumentException("rule must be vertex header")
      }
    }
    mch.success
  }

  /**
   * マッチした可能性のうち一つを選択します
   * @param pack すべてのパターンマッチのパターン
   * @return そのうち1つのパターンマッチ
   */
  def pick(pack: BindingPack): Binding = {
    pack.pickFirst
  }

  /**
   * 得られたグラフの要素の対応を元に新しいグラフをルール右辺を元に
   * 構築します
   * @param binding パターンマッチしたBinding
   * @return 新規に構築されたグラフ
   */
  def build(binding: Binding): Vertex = {
    val context = new BuildingContext(binding)
    rule.rhs.build(context)
  }

  /**
   * 書き換え可能な部分グラフがあれば，1回のみ書き換え，書き換えたかどうかを返します
   * 書き換え可能な部分グラフが存在しない場合は，何も行わず`false`を返します
   * @param reserve 書き換える対象の`Reserve`
   * @return 実際に書き換えが行われたかどうか
   */
  def step(reserve: Reserve): Boolean = {
    for (vertex <- reserve.iterVertices) {
      val rewrote = this.begin(vertex)
      if (rewrote) {
        return true
      }
    }
    false
  }

  /**
   * Reserveに対し書き換えが停止するまで書き換えを行います
   * @param reserve
   */
  def rewrite(reserve: Reserve) = {

  }

}
