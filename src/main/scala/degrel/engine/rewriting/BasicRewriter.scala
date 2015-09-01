package degrel.engine.rewriting

import degrel.core._
import degrel.utils.PrettyPrintOptions

/**
 * グラフのルールを元に書き換えを実行します
 */
abstract class BasicRewriter extends Rewriter {
  self =>
  def rule: Rule

  protected def getBinding(pack: BindingPack, cellBinding: Binding): Binding

  lazy val isSpawnsCells = Traverser(rule.rhs).exists(_.isCell)


  override def pattern: Vertex = rule.lhs

  /**
   * この書き換え機で`target`を書き換えます．
   * @return 書き換えが実行された場合は`true`，何も行われなかった場合は`false`
   * @todo Ruleなrhsをbuildして書き込んでしまうと継続の際のbindingが壊れてしまうため
   *       とりあえず参照を書き込む．
   *       --> 参照経由で規則が書き換えられてしまう可能性・・・・
   */
  def rewrite(rt: RewritingTarget): RewriteResult = {
    val target = rt.target
    val self = rt.self
    val diagnostics = self.chassis.diagnostics

    val mch = diagnostics.matchSpan.enter {
      target.matches(rule.lhs)
    }

    if (mch.success) {
      val binding = this.getBinding(mch.pack, self.binding)
      if (rule.rhs.isRule) {
        RewriteResult.Continue(rt, rule.rhs.asRule, binding)
      } else {
        val builtGraph = diagnostics.buildSpan.enter {
          molding.mold(rule.rhs, binding, self)
        }
        write(rt, builtGraph)
      }
    } else {
      nop
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
