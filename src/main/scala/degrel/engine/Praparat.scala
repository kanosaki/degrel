package degrel.engine

import degrel.DegrelException
import degrel.core._
import degrel.core.utils.PrettyPrintOptions
import degrel.engine.rewriting.Rewriter

import scala.collection.mutable

/**
 * Cellの実行をします
 */
class Praparat(val cell: Cell) extends Reactor {
  def rewriters = cell.rules.map(Rewriter(_))

  private var contRewriters: mutable.Buffer[Rewriter] = mutable.ListBuffer()
  implicit protected val printOption = PrettyPrintOptions(showAllId = true, multiLine = true)

  /**
   * 1回書き換えます
   *
   * 1. 探索を実行する規則の選択
   * 2. 探索の実行
   * 3. 書き換えの実行
   */
  def step(): Boolean = {
    for (rw <- contRewriters ++ this.rewriters) {
      for (v <- this.rewriteTargets) {
        try {
          if (this.execRewrite(rw, v)) {
            return true
          }
        } catch {
          case e: Throwable => {
            throw e
          }
        }
      }
    }
    false
  }

  private def execRewrite(rw: Rewriter, v: Vertex): Boolean = {
    val res = rw.rewrite(v)
    if (res.done) {
      import degrel.engine.rewriting.Continuation._
      res.continuation match {
        case c@HasNext(nextRule, _) => {
          contRewriters += Rewriter(nextRule, Some(c))
          cell.removeRoot(v)
        }
        case Empty => contRewriters -= rw
      }
    }
    res.done
  }

  def rewriteTargets: Iterable[Vertex] = {
    val roots = cell
      .edges
      .filter(_.label != Label.E.cellRule)
      .map(_.dst)
      .filter(_.label != Label.V.rule)
    roots.flatMap(Traverser(_, edgePred = _.dst.label != Label.V.cell))
  }

  def stepUntilStop(limit: Int = -1): Int = {
    var count = 0
    while (true) {
      val rewrote = this.step()
      count += 1
      if (!rewrote) return count
      if (limit > 0 && count > limit) {
        throw DegrelException("Exec limitation exceeded.")
      }
    }
    count
  }
}
