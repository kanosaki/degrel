package degrel.engine

import degrel.DegrelException
import degrel.core._
import degrel.engine.rewriting._
import degrel.engine.sphere.Sphere
import degrel.utils.PrettyPrintOptions

import scala.collection.mutable

/**
 * Cellの実行をします
 */
class Driver(val header: Vertex, val chassis: Chassis, val parent: Driver = null) extends Reactor {
  implicit val printOption = PrettyPrintOptions(multiLine = true)
  private var children = new mutable.HashMap[Vertex, Driver]()
  private var contRewriters: mutable.Buffer[ContinueRewriter] = mutable.ListBuffer()
  var rewritee: RewriteeSet = new PlainRewriteeSet(this)
  private var rewriteTryCount: Long = 0

  def isActive: Boolean = {
    header.isCell && this.cell.edges.nonEmpty
  }

  def wholeTryCount: Long = {
    rewriteTryCount + children.valuesIterator.foldLeft(0l)(_ + _.wholeTryCount)
  }

  val resource: Sphere = if (chassis == null) {
    degrel.engine.sphere.default
  } else {
    chassis.getResourceFor(this)
  }

  def stepUntilStop(limit: Int = -1): Int = {
    var count = 0
    while (true) {
      if (chassis.verbose) {
        System.err.print(Console.RED)
        System.err.println("--- Graph ---")
        System.err.println(this.header.pp)
        System.err.print(Console.YELLOW)
        System.err.println("--- Continuations ---")
        this.contRewriters.foreach(rw => {
          System.err.println(rw.pp)
        })
        System.err.println(Console.RESET)
      }
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
    atoms.foreach(r => {
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
    contRewriters.find(this.stepFor) match {
      case Some(rw) => {
        this.cell.removeRoot(rw.tempVertex)
        contRewriters -= rw
        true
      }
      case None => {
        this.rewriters.exists(this.stepFor)
      }
    }
  }

  def stepFor(rw: Rewriter): Boolean = {
    val targets = this.rewritee.targetsFor(rw)
//    val allTargets = this.rewriteTargets.toSet
//    if (targets.size != allTargets.size) {
//      println("----------------------------------")
//      println(s"${targets.size} / ${allTargets.size}")
//      if (allTargets.size > 200) {
//        println(rw.pp)
//        targets.foreach { t =>
//          println(s"${t.target.pp}")
//        }
//      }
//    }
    targets.exists { rc =>
      this.execRewrite(rw, rc)
    }
  }

  def baseRewriters: Seq[Rewriter] = cell.bases.flatMap(_.rules.map(Rewriter(_)))

  def rewriters: Seq[Rewriter] = cell.rules.map(Rewriter(_)) ++ baseRewriters ++ degrel.primitives.rewriter.default

  def atoms: Iterable[Vertex] = cell.roots

  def atomTargets: Iterable[RewritingTarget] = cell.roots.map { r =>
    RewritingTarget(r.asHeader, r.asHeader, this)
  }

  def rewriteTargets: Iterable[RewritingTarget] = {
    CellTraverser(this.cell, this)
  }

  /**
   * Send message vertex underlying cell
   */
  def send(msg: Vertex) = {
    this.addRoot(this.cell, msg)
  }

  def cell: CellBody = header.unhead[CellBody]

  def spawn(cell: Vertex): Vertex = {
    this.children += cell -> chassis.createDriver(cell, this)
    cell
  }

  def cleanup(): Unit = {
    if (this.isActive) {
      this.children = this.children.filter(_._2.isActive)
      this.cell.roots.filter(v => {
        v.isCell && v.edges.isEmpty
      }).foreach { v =>
        this.removeRoot(v)
      }
    }
  }

  private def execRewrite(rw: Rewriter, rc: RewritingTarget): Boolean = {
    rewriteTryCount += 1
    val res = rw.rewrite(rc)
    if (res.done) {
      res.exec(this)
      if (chassis.verbose) {
        System.err.print(Console.GREEN)
        System.err.println("--- Apply ---")
        System.err.println(rw.pp)
        System.err.print(Console.BLUE)
        System.err.println("--- Result ---")
        System.err.println(this.header.pp)
        System.err.println(Console.RESET)
      }
    }
    res.done
  }

  def addContinueRewriter(rw: ContinueRewriter) = {
    this.rewritee.onContinue(rw)
    contRewriters += rw
  }

  def writeVertex(target: RewritingTarget, value: Vertex): Unit = {
    if (target.target == this.header) {
      if (this.parent == null) {
        throw DegrelException("Destroying root cell!")
      } else {
        this.parent.writeVertex(target, value)
      }
    }
    this.rewritee.onWriteVertex(target, value)
    target.target.write(value)
  }

  def removeRoot(v: Vertex): Unit = {
    this.rewritee.onRemoveRoot(v)
    this.cell.removeRoot(v)
  }

  def addRoot(target: Cell, value: Vertex) = {
    if (value.isCell) {
      this.spawn(value.asCell)
    }
    target.addRoot(value)
  }

  def binding: Binding = {
    if (this.header.isCell) {
      this.cell.binding
    } else {
      Binding.empty()
    }

  }
}

object Driver {
  def apply(): Driver = {
    Driver(Cell())
  }

  def apply(cell: Cell): Driver = {
    new Driver(cell, Chassis.create())
  }
}
