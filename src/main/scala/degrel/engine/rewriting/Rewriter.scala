package degrel.engine.rewriting

import degrel.Logger
import degrel.core._
import degrel.engine.rewriting.molding.MoldingContext


/**
 * 書き換えを実行します
 */
trait Rewriter extends Logger {
  self =>
  def rule: Rule

  lazy val spawnsCells = Traverser(rule.rhs).exists(_.isCell)

  /**
   * この書き換え機で`target`を書き換えます．
   * @param target 書き換える対象のグラフ
   * @return 書き換えが実行された場合は`true`，何も行われなかった場合は`false`
   * @todo Ruleなrhsをbuildして書き込んでしまうと継続の際のbindingが壊れてしまうため
   *       とりあえず参照を書き込む．
   *       --> 参照経由で規則が書き換えられてしまう可能性・・・・
   */
  def rewrite(target: Vertex): RewriteResult = {
    val mch = target.matches(rule.lhs)
    if (mch.success) {
      val binding = this.pick(mch.pack)
      val vh = target.asInstanceOf[VertexHeader]
      if (rule.rhs.isRule) {
        val cont = Continuation.HasNext(rule.rhs.asRule, binding)
        RewriteResult(done = true, cont)
      } else {
        val builtGraph = molding.mold(rule.rhs, binding)
        vh.write(builtGraph)
        RewriteResult(done = true)
      }
    } else {
      RewriteResult(done = false)
    }
  }

  protected def getBinding(pack: BindingPack): Binding

  def build(target: Vertex): Option[Vertex] = {
    val mch = target.matches(rule.lhs)
    if (mch.success) {
      val binding = this.pick(mch.pack)
      Some(molding.mold(rule.rhs, binding))
    } else {
      None
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
}

object Rewriter {
  def apply(v: Vertex, contOpt: Option[Continuation] = None): Rewriter = {
    contOpt match {
      case Some(cont) => new ContinueRewriter(v.asRule, cont)
      case None => new NakedRewriter(v.asRule)
    }
  }
}
