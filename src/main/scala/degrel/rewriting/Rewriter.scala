package degrel.rewriting

import degrel.Logger
import degrel.core._
import degrel.utils.collection.ShuffledIterator


/**
 * 書き換えを実行します
 */
class Rewriter(val rule: Rule) extends Logger {
  self =>
  /**
   * targetに指定された頂点を根としてパターンマッチを行い，マッチすれば書き換えを行います
   * 書き換えを行った場合はtrueを，書き換えを行わなかった場合はfalseを返します
   * @param target このルールで書き換えるグラフの頂点
   * @return 書き換えが実行された場合はTrue, 実行されなければFalse
   */
  def rewriteTransaction(root: Vertex, target: Vertex): ActionLog = {
    val mch = target.matches(rule.lhs)
    if (mch.success) {
      implicit val transaction = new Transaction()
      val targetHeader = target.asInstanceOf[VertexHeader]
      val (prevLocator, newLocator) = targetHeader.beginTransaction()
      val binding = this.pick(mch.pack)
      val builtGraph = this.buildFromBinding(binding)
      val builtSucceeded = binding.confirm() && newLocator.tryCommit(builtGraph)
      if (!builtSucceeded) {
        return new BuildingFailure(root, target, newLocator.oldVertex, newLocator.newVertex)
      }
      val commitSucceeded = targetHeader.commitTransaction(prevLocator, newLocator) && transaction.complete()
      if (!commitSucceeded) {
        return new CommitingFailure(root, target, newLocator.oldVertex, newLocator.newVertex)
      }
      new RewritingSucceed(root, target, newLocator.oldVertex, newLocator.newVertex)
    } else {
      NOP
    }
  }

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
      val builtGraph = this.buildFromBinding(binding)
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

  /**
   * 得られたグラフの要素の対応を元に新しいグラフをルール右辺を元に
   * 構築します
   * @param binding パターンマッチしたBinding
   * @return 新規に構築されたグラフ
   */
  protected def buildFromBinding(binding: Binding): Vertex = {
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
    for (rt <- ShuffledIterator(reserve.roots.iterator)) {
      for (vertex <- Traverser(rt)) {
        val result = this.rewriteTransaction(rt, vertex)
        if (result.performed) {
          logger.debug(result.repr)
        }
        if (result.succeed) {
          return true
        }
      }
    }
    false
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
