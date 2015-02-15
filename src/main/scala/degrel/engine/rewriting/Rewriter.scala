package degrel.engine.rewriting

import degrel.Logger
import degrel.core._
import degrel.utils.collection.ShuffledIterator


/**
 * 書き換えを実行します
 */
class Rewriter(val rule: Rule) extends Logger {
  self =>
  /**
   * この書き換え機で`target`を書き換えます．
   * @param target 書き換える対象のグラフ
   * @return 書き換えが実行された場合は`true`，何も行われなかった場合は`false`
   */
  def rewrite(target: Vertex): Boolean = {
    this.build(target) match {
      case Some(builtGraph) => {
        val vh = target.asInstanceOf[VertexHeader]
        vh.write(builtGraph)
        true
      }
      case None => false
    }
  }

  /**
   * この書き換え機で`target`を根とするグラフに対しパターンマッチと構築を行います
   * このメソッドでは`target`に対し影響を与えません
   * @param target 対象となるグラフ
   * @return パターンマッチに成功し，グラフ構築に成功した場合はその構築されたグラフが
   *         どちらかに失敗した場合は`None`が返されます
   */
  def build(target: Vertex): Option[Vertex] = {
    val mch = target.matches(rule.lhs)
    if (mch.success) {
      val binding = this.pick(mch.pack)
      val context = new BuildingContext(binding)
      val builtGraph = rule.rhs.build(context)
      Some(builtGraph)
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

  abstract class ActionLog(val succeed: Boolean, val root: Vertex, val target: Vertex, prev: VertexBody, next: VertexBody) {
    def rule = self.rule

    def performed: Boolean

    def msg: String

    def repr = s"$msg:\n  Rule  : $rule\n  Target: $target\n  Root  : $root\n  Prev  : $prev\n  Next  : $next"
  }

  class BuildingFailure(root: Vertex, target: Vertex, prev: VertexBody, next: VertexBody)
    extends ActionLog(false, root, target, prev, next) {
    def msg = "BuildingFailure"

    def performed = true
  }

  class CommitingFailure(root: Vertex, target: Vertex, prev: VertexBody, next: VertexBody)
    extends ActionLog(false, root, target, prev, next) {
    def msg = "CommitingFailure"

    def performed = true
  }

  class RewritingSucceed(root: Vertex, target: Vertex, prev: VertexBody, next: VertexBody)
    extends ActionLog(true, root, target, prev, next) {
    def msg = "RewritingSucceed"

    def performed = true
  }

  object NOP extends ActionLog(false, null, null, null, null) {
    def msg = "NOP"

    override def repr = "NOP"

    def performed = false
  }

}
