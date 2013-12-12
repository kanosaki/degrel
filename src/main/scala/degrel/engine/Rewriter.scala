package degrel.engine

import scala.util.control.Breaks
import degrel.core.{VertexHeader, Rule, Vertex}

/**
 * 書き換えの手続きを管理します
 * @todo トランザクションに対応
 */
class Rewriter(rule: Rule) {
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

  def step(reserve: Reserve) = {
    val b = new Breaks
    var ret = false
    b.breakable {
      for (vertex <- reserve.iterVertices) {
        val rewrote = this.begin(vertex)
        if (rewrote) {
          ret = true
          b.break
        }
      }
    }
    ret
  }
}
