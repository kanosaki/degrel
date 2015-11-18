package degrel.engine

import degrel.DegrelException
import degrel.cluster.LocalNode
import degrel.core._
import degrel.core.transformer.{CellLimiter, GraphVisitor, TryOwnVisitor}
import degrel.engine.rewriting._
import degrel.engine.sphere.Sphere
import degrel.utils.PrettyPrintOptions

import scala.collection.mutable

/**
  * Cellの実行をします
  */
class LocalDriver(val header: Vertex, val chassis: Chassis, val node: LocalNode, val parent: Driver) extends Reactor with Driver {1
  implicit val printOption = PrettyPrintOptions(multiLine = true)
  private var children = new mutable.HashMap[Vertex, Driver]()
  private var contRewriters: mutable.Buffer[ContinueRewriter] = mutable.ListBuffer()
  var tryOwnVisitor: GraphVisitor = GraphVisitor(
    CellLimiter.default,
    new TryOwnVisitor(this.header))

  override def isActive: Boolean = {
    header.isCell && this.cell.edges.nonEmpty
  }

  override val resource: Sphere = if (chassis == null) {
    degrel.engine.sphere.default
  } else {
    chassis.getResourceFor(this)
  }

  override def stepUntilStop(limit: Int = -1): Int = {
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

  override def stepRecursive(): Boolean = {
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
    this.atoms.exists { atom =>
      val stepFunc = this.stepFor(atom) _
      contRewriters.find(stepFunc) match {
        case Some(rw) => {
          this.cell.removeRoot(rw.tempVertex)
          contRewriters -= rw
          true
        }
        case None => {
          this.rewriters.exists(stepFunc)
        }
      }
    } || this.rewriters.filter(_.isMeta).exists { rw =>
      val target = RewritingTarget.alone(this.header, this)
      this.execRewrite(rw, target)
    }
  }

  def stepFor(atom: Vertex)(rw: Rewriter) = {
    val targets = if (rw.isPartial) {
      CellTraverser(atom, this)
    } else {
      Seq(RewritingTarget.alone(atom, this))
    }
    targets.exists { rc =>
      this.execRewrite(rw, rc)
    }
  }

  //def stepFor(rw: Rewriter): Boolean = {
  //  val targets = this.rewritee.targetsFor(rw)
  //  targets.exists { rc =>
  //    this.execRewrite(rw, rc)
  //  }
  //}

  override def rewriters: Seq[Rewriter] = selfRewriters ++ baseRewriters ++ parent.rewriters

  def atoms: Iterable[Vertex] = cell.roots

  def atomTargets: Iterable[RewritingTarget] = cell.roots.map { r =>
    RewritingTarget(r.asHeader, r.asHeader, this)
  }

  def rewriteTargets: Iterable[RewritingTarget] = {
    CellTraverser(this.cell, this)
  }

  override def spawn(cell: Vertex): Driver = {
    val drv = chassis.createDriver(cell, this)
    this.children += cell -> drv
    drv
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
    val res = chassis.diagnostics.rewriteSpan.enter {
      rw.rewrite(rc)
    }

    if (res.done) {
      chassis.diagnostics.applySpan.enter {
        res.exec(this)
      }
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
    contRewriters += rw
  }

  override def writeVertex(target: RewritingTarget, value: Vertex): Unit = {
    if (target.target == this.header && this.parent != null) {
      this.parent.writeVertex(target, value)
    } else {
      tryOwnVisitor.visit(value)
      target.target.write(value)
    }
  }

  override def removeRoot(v: Vertex): Unit = {
    this.cell.removeRoot(v)
  }

  override def dispatch(target: Cell, value: Vertex) = {
    if (value.isCell) {
      this.spawn(value.asCell)
    }
    if (target == this.header) {
      tryOwnVisitor.visit(value)
    }
    target.addRoot(value)
  }

  override def binding: Binding = {
    if (this.header.isCell) {
      this.cell.binding
    } else {
      Binding.empty()
    }
  }

  override def getVertex(id: ID): Option[Vertex] = {
    CellTraverser(this.header, this).find(_.target.id == id).map(_.target)
  }
}

object LocalDriver {
  def apply(): LocalDriver = {
    LocalDriver(Cell())
  }

  def apply(cell: Cell, chassis: Chassis = null): LocalDriver = {
    val chas = if (chassis == null) Chassis.create() else chassis
    val node = LocalNode.current
    new RootLocalDriver(cell, chas, node)
  }

}

class RootLocalDriver(_header: Vertex, _chassis: Chassis, _node: LocalNode) extends LocalDriver(_header, _chassis, _node, null) {
  override def rewriters: Seq[Rewriter] = selfRewriters ++ baseRewriters ++ degrel.primitives.rewriter.default
}
