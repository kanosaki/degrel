package degrel.engine.rewriting

import degrel.Logger
import degrel.core._
import degrel.engine.Driver
import degrel.engine.rewriting.molding.MoldingContext
import degrel.utils.PrettyPrintable


/**
 * 書き換えを実行します
 * 基本的にグラフの規則を元に書き換えを実行しますが，一部規則は
 * ネイティブ実装されます．
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
  def rewrite(target: VertexHeader, parent: Driver): RewriteResult

  def rewrite(target: VertexHeader): RewriteResult = {
    this.rewrite(target, Driver())
  }

  def isMeta: Boolean = false

  def isPartial: Boolean = true
}

object Rewriter {
  def apply(v: Vertex, contOpt: Option[Continuation] = None): Rewriter = {
    contOpt match {
      case Some(cont) => new ContinueRewriter(v.asRule, cont)
      case None => new NakedRewriter(v.asRule)
    }
  }
}
