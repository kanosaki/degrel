package degrel.engine

import degrel.DegrelException
import degrel.core._
import degrel.core.utils.PrettyPrintOptions
import degrel.engine.rewriting.Rewriter

import scala.collection.mutable

/**
 * Cellの実行をします
 */
class Driver(val cell: Cell) extends Reactor {
  implicit protected val printOption = PrettyPrintOptions(showAllId = true, multiLine = true)
  private val children: mutable.Buffer[Driver] = mutable.ListBuffer()
  private var contRewriters: mutable.Buffer[Rewriter] = mutable.ListBuffer()

  def rewriters = cell.rules.map(Rewriter(_)) ++ degrel.builtins.rewriter.default

  /**
   * 1回書き換えます
   *
   * 1. 探索を実行する規則の選択
   * 2. 探索の実行
   * 3. 書き換えの実行
   */
  def step(): Boolean = {
    val applicativeRewriters = contRewriters ++ this.rewriters
    applicativeRewriters.exists { rw =>
      this.rewriteTargets.exists { v =>
        this.execRewrite(rw, v)
      }
    }
  }

  def stepRecursive(): Boolean = {
    this.children.find(_.stepRecursive()) match {
      case Some(_) => true
      case None => this.step()
    }
  }

  def rewriteTargets: Iterable[Vertex] = {
    val roots = cell
      .edges
      .filter(_.label == Label.E.cellItem)
      .map(_.dst)
    roots.flatMap(Traverser(_, TraverserCutOff(_.label == Label.V.cell, TraverseRegion.InnerOnly)))
  }

  def stepUntilStop(limit: Int = -1): Int = {
    var count = 0
    while (true) {
      val rewrote = this.stepRecursive()
      count += 1
      if (!rewrote) return count
      if (limit > 0 && count > limit) {
        throw DegrelException(s"Exec limitation exceeded. \n${cell.pp}")
      }
    }
    count
  }

  /**
   * Send message vertex underlying cell
   */
  def send(msg: Vertex) = {
    this.cell.addRoot(msg)
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
      if (rw.isSpawnsCells) {
        val spawnedCells = Traverser(v, _.isCell, TraverseRegion.WallOnly)
        children ++= spawnedCells.map(_.asCell).map(new Driver(_))
      }
    }
    res.done
  }
}
