package degrel.engine.rewriting

import degrel.Logger
import degrel.core._
import degrel.engine.Driver
import degrel.utils.PrettyPrintable


/**
 * 書き換えを実行します
 * 基本的にグラフの規則を元に書き換えを実行しますが，一部規則は
 * ネイティブ実装されます．
 *
 * 書き換えの`target`としては，通常`Cell`の要素の根が渡されます．
 * すなわち，`cell.thru(__item__)`です
 *
 * ただし，`isPartial`が`true`だと，さらにその子孫要素を`Travrese`し
 * 次の`cell`等この`Cell`の管理外になるまで`target`を渡します
 * また，`isMeta`が`true`だと，この`cell`自身も`target`として渡されます
 */
trait Rewriter extends Logger with PrettyPrintable {
  self =>
  /**
   * この書き換え機で`target`を書き換えます．
   * @param target 書き換える対象のグラフ
   * @return 書き換えが実行された場合は`true`，何も行われなかった場合は`false`
   * @todo Ruleなrhsをbuildして書き込んでしまうと継続の際のbindingが壊れてしまうため
   *       とりあえず参照を書き込む．
   *       --> 参照経由で規則が書き換えられてしまう可能性・・・・
   */
  def rewrite(self: Driver, target: VertexHeader): RewriteResult

  def rewrite(target: VertexHeader): RewriteResult = {
    this.rewrite(Driver(), target)
  }

  /**
   * 書き換え対象として，この規則を含むCellも有効になります
   */
  def isMeta: Boolean = false

  /**
   * 書き換え対象として，Cellのitemsを再帰的に辿って渡されます
   */
  def isPartial: Boolean = true
}

object Rewriter {
  def apply(v: Vertex): Rewriter = {
    new NakedRewriter(v.asRule)
  }

  def parse(expr: String): Rewriter = {
    val v = degrel.parseVertex(expr)
    new NakedRewriter(v.asRule)
  }
}
