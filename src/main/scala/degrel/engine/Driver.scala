package degrel.engine

import degrel.DegrelException
import degrel.core._
import degrel.engine.resource.Resource
import degrel.engine.rewriting.Rewriter
import degrel.utils.PrettyPrintOptions

import scala.collection.mutable

/**
 * Cellの実行をします
 */
class Driver(val header: Vertex) extends Reactor {
  val resource: Resource = degrel.engine.resource.default
  implicit protected val printOption = PrettyPrintOptions(showAllId = true, multiLine = true)
  private var children = new mutable.HashMap[Vertex, Driver]()
  private var contRewriters: mutable.Buffer[Rewriter] = mutable.ListBuffer()

  def isActive: Boolean = {
    header.isCell && this.cell.edges.nonEmpty
  }

  def stepUntilStop(limit: Int = -1): Int = {
    var count = 0
    while (true) {
      val rewrote = this.stepRecursive()
      this.cleanup()
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
      if (r.isCell) {
        if (!children.contains(r)) {
          this.spawn(r)
        }
      } else {
        r.thru(0).filter(_.isCell).foreach { c =>
          if (!children.contains(c)) {
            this.spawn(c)
          }
        }
      }
    })
    this.children.values.find(_.stepRecursive()) match {
      case Some(_) => true
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
      val targets =
        (if (rw.isPartial)
          this.rewriteTargets
        else
          this.itemRoots) ++
          (if (rw.isMeta)
            Seq(header)
          else
            Seq())
      targets.exists { v =>
        this.execRewrite(rw, v)
      }
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

  def cell: CellBody = header.unref[CellBody]

  def spawn(cell: Vertex): Vertex = {
    this.children += cell -> new Driver(cell)
    cell
  }

  def cleanup(): Unit = {
    if (this.isActive) {
      this.children = this.children.filter(_._2.isActive)
      this.cell.roots.filter(v => {
        v.isCell && v.edges.isEmpty
      }).foreach { v =>
        this.cell.removeRoot(v)
      }
    }
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
