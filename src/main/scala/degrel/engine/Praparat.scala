package degrel.engine

import degrel.DegrelException
import degrel.core.{Cell, Label, Traverser, Vertex}
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
    val roots = cell
      .edges
      .filter(_.label != Label.E.cellRule)
      .map(_.dst)
    roots.flatMap(Traverser(_, edgePred = _.dst.label != Label.V.cell))
  }

  def stepUntilStop(limit: Int = -1): Int = {
    var count = 0
    while (true) {
      val rewrote = this.step()
      count += 1
      if (!rewrote) return count
      if(limit > 0 && count > limit) {
        throw new DegrelException("Exec limitation exceeded.")
      }
    }
    count
  }
}
