package degrel.engine.rewriting

import degrel.core._
import degrel.engine.Driver
import degrel.utils.PrettyPrintOptions

/**
 * グラフのルールを元に書き換えを実行します
 */
abstract class BasicRewriter extends Rewriter {
  self =>
  def rule: Rule
  protected def getBinding(pack: BindingPack): Binding

  lazy val isSpawnsCells = Traverser(rule.rhs).exists(_.isCell)

  /**
   * この書き換え機で`target`を書き換えます．
   * @param target 書き換える対象のグラフ
   * @return 書き換えが実行された場合は`true`，何も行われなかった場合は`false`
   * @todo Ruleなrhsをbuildして書き込んでしまうと継続の際のbindingが壊れてしまうため
   *       とりあえず参照を書き込む．
   *       --> 参照経由で規則が書き換えられてしまう可能性・・・・
   */
  def rewrite(target: VertexHeader, parent: Driver): RewriteResult = {
    val mch = target.matches(rule.lhs)
    if (mch.success) {
      val binding = this.getBinding(mch.pack)
      if (rule.rhs.isRule) {
        val cont = Continuation.Continue(rule.rhs.asRule, binding)
        RewriteResult(done = true, cont)
      } else {
        val builtGraph = molding.mold(rule.rhs, binding, parent)
        target.write(builtGraph)
        RewriteResult(done = true)
      }
    } else {
      RewriteResult.NOP
    }
  }

  /**
   * マッチした可能性のうち一つを選択します
   * @param pack すべてのパターンマッチのパターン
   * @return そのうち1つのパターンマッチ
   */
  protected def pick(pack: BindingPack): Binding = {
    pack.pickFirst
  }

  override def pp(implicit opt: PrettyPrintOptions): String = {
    this.rule.pp
  }
}
