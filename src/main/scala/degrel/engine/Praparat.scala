package degrel.engine

import degrel.core.{Traverser, Cell, Label, Vertex}
import degrel.engine.rewriting.Rewriter

/**
 * Cellの実行をします
 */
class Praparat(val cell: Cell) extends Reactor {
  private[this] val rewriters: Seq[Rewriter] = cell.rules.map(r => new Rewriter(r))

  /**
   * 1回書き換えます
   *
   * 1. 探索を実行する規則の選択
   * 2. 探索の実行
   * 3. 書き換えの実行
   */
  def step(): Boolean = {
    for (rw <- this.rewriters) {
      for (v <- this.rewriteTargets) {
        val res = rw.rewrite(v)
        if (res) {
          return true
        }
      }
    }
    false
  }

  def rewriteTargets: Iterable[Vertex] = {
    val roots = cell.
      edges().
      filter(_.label != Label.V.rule).
      map(_.dst)
    roots.flatMap(Traverser(_))
  }
}
