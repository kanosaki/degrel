package degrel.engine

import degrel.DegrelException
import degrel.core._
import degrel.engine.rewriting.Rewriter
import degrel.utils.PrettyPrintOptions

import scala.collection.mutable

/**
 * Cellの実行をします
 */
class Driver(val cell: Cell) extends Reactor {
  implicit protected val printOption = PrettyPrintOptions(showAllId = true, multiLine = true)
  private val children: mutable.Buffer[Driver] = mutable.ListBuffer()
  private var contRewriters: mutable.Buffer[Rewriter] = mutable.ListBuffer()

  def rewriters = cell.rules.map(Rewriter(_)) ++ degrel.primitives.rewriter.default

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
      val res = this.rewriteTargets.exists { v =>
        this.execRewrite(rw, v)
      }
      if (rw.isMeta) {
        this.execRewrite(rw, this.cell)
      }
      res
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

  def spawn(cell: Cell): Cell = {
    this.children += new Driver(cell)
    cell
  }

  private def execRewrite(rw: Rewriter, v: Vertex): Boolean = {
    val res = rw.rewrite(v.asHeader, this)
    if (res.done) {
      import degrel.engine.rewriting.Continuation._
      res.continuation match {
        case c@Continue(nextRule, _) => {
          contRewriters += Rewriter(nextRule, Some(c))
          cell.removeRoot(v)
        }
        case Empty => contRewriters -= rw
      }
    }
    res.done
  }
}

object Driver {
  def apply(): Driver = {
    Driver(Cell())
  }

  def apply(cell: Cell): Driver = {
    new Driver(cell)
  }
}
