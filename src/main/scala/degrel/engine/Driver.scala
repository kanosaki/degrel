package degrel.engine

import degrel.DegrelException
import degrel.core._
import degrel.engine.rewriting.Rewriter
import degrel.utils.PrettyPrintOptions

import scala.collection.mutable

/**
 * Cellの実行をします
 */
class Driver(val header: Vertex) extends Reactor {
  implicit protected val printOption = PrettyPrintOptions(showAllId = true, multiLine = true)
  private var children = new mutable.HashMap[Vertex, Driver]()
  private var contRewriters: mutable.Buffer[Rewriter] = mutable.ListBuffer()

  def cell: CellBody = header.unref[CellBody]

  def isActive: Boolean = {
    header.isCell
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

  def stepRecursive(): Boolean = {
    if (!this.isActive) {
      return false
    }
    itemRoots.foreach(r => {
      Traverser(r, TraverserCutOff(_.label == Label.V.cell, TraverseRegion.WallOnly)).foreach(neighborCell => {
        if (!children.contains(neighborCell)) {
          if (!neighborCell.isInstanceOf[Cell]) {
            println("BREAK")
          }
          this.spawn(neighborCell)
        }
      })
    })
    this.children.values.find(_.stepRecursive()) match {
      case Some(c) => {
        if (!c.isActive) {
          children -= c.header
        }
        true
      }
      case None => this.step()
    }
  }

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
      // only meta rewriters can rewrite self cell
      if (rw.isMeta) {
        this.execRewrite(rw, header)
        if (!this.isActive) {
          // rewrite cell by fin rule.
          println(s"FIN: $header")
        }
      }
      res
    }
  }

  def rewriters = cell.rules.map(Rewriter(_)) ++ degrel.primitives.rewriter.default

  def itemRoots: Iterable[Vertex] = cell
    .edges
    .filter(_.label == Label.E.cellItem)
    .map(_.dst)

  def rewriteTargets: Iterable[Vertex] = {
    this.itemRoots.flatMap(Traverser(_, TraverserCutOff(_.label == Label.V.cell, TraverseRegion.InnerOnly)))
  }

  /**
   * Send message vertex underlying cell
   */
  def send(msg: Vertex) = {
    if (msg.isCell) {
      this.spawn(msg.asCell)
    }
    this.cell.addRoot(msg)
  }

  def spawn(cell: Vertex): Vertex = {
    println(s"SPAWN: \n ${cell.pp}")
    this.children += cell -> new Driver(cell)
    cell
  }

  private def execRewrite(rw: Rewriter, v: Vertex): Boolean = {
    val prev = v.pp
    val res = rw.rewrite(v.asHeader, this)
    if (res.done) {
      println(s"APPLY: \n${rw.pp}")
      println(s" PREV: \n${prev}")
      println(s"   TO: \n${v.pp}")
      println(s" CMAP: \n${this.children}")
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
