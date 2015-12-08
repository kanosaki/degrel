package degrel.engine

import degrel.DegrelException
import degrel.cluster.{LocalNode, Timeouts}
import degrel.core._
import degrel.core.transformer.{FixIDVisitor, GraphVisitor, TransferOwnerVisitor}
import degrel.engine.rewriting._
import degrel.engine.sphere.Sphere
import degrel.utils.PrettyPrintOptions

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Cellの実行をします
  */
class LocalDriver(val header: VertexHeader,
                  val chassis: Chassis,
                  val node: LocalNode,
                  val returnTo: VertexPin,
                  val parent: Option[Driver]) extends Reactor with Driver {
  implicit val printOption = PrettyPrintOptions(multiLine = true, showAllId = true)
  private var children = new mutable.HashMap[ID, Driver]()
  private var contRewriters: mutable.Buffer[ContinueRewriter] = mutable.ListBuffer()
  val idSpace = node.nextIDSpace()
  chassis.verbose = true
  this.init()

  def isValid: Boolean = {
    header.isCell && this.cell.edges.nonEmpty
  }

  override val resource: Sphere = if (chassis == null) {
    degrel.engine.sphere.default
  } else {
    chassis.getResourceFor(this)
  }

  override def stepUntilStop(limit: Int = -1): Int = {
    if (this.isStopped) {
      throw new RuntimeException("Already stopped!")
    }
    var count = 0
    if (chassis.verbose) {
      println(s"$id (on: ${node.selfID})>>>>>")
      println(s"DRIVER_START: $id >>>>>")
      println("--- Parent ---")
      println(s"$parent")
      //System.err.println("--- Rewriters ---")
      //this.rewriters.foreach { rw =>
      //  System.err.println(rw.pp)
      //}
    }
    while (!state.isStopped) {
      if (chassis.verbose) {
        print(Console.RED)
        println(s"ID: $id steps: $count")
        println("--- Graph ---")
        println(this.header.pp)
        print(Console.YELLOW)
        println("--- Continuations ---")
        this.contRewriters.foreach(rw => {
          println(rw.pp)
        })
        println(Console.RESET)
        println("--- Children ---")
        this.children.values.foreach(c => {
          println(c)
        })
      }
      val rewrote = this.stepRecursive()
      this.cleanup()
      count += 1
      if (!rewrote) {
        state = DriverState.Paused(count)
        return count
      }
      if (limit > 0 && count > limit) {
        throw DegrelException(s"Exec limitation exceeded. \n${cell.pp}")
      }
    }
    count
  }

  def init(): Unit = {
    this.cleanup()
    this.header.updateID(idSpace.next())
    node.registerDriver(this.id.ownerID, this)
    val fixIDVisitor = GraphVisitor(Traverser.apply(_), new FixIDVisitor(idSpace))
    this.header.edgesWith(Label.E.cellItem).map(_.dst).foreach(fixIDVisitor.visit)
    this.header.edgesWith(Label.E.cellRule).map(_.dst).foreach(fixIDVisitor.visit)
    this.prepare()
  }

  def prepare(): Unit = {
    atoms.foreach(r => {
      if (r.isCell) {
        if (!children.contains(r.id)) {
          this.spawn(r)
        }
      } else {
        r.thru(0).filter(_.isCell).foreach { c =>
          if (!children.contains(c.id)) {
            this.spawn(c)
          }
        }
      }
    })
  }

  override def stepRecursive(): Boolean = {
    if (!this.isValid) {
      return false
    }
    this.prepare()
    this.children.values.find {
      case ld: LocalDriver => ld.stepRecursive()
      case _ => false
    } match {
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

  override def rewriters: Seq[Rewriter] = selfRewriters ++ baseRewriters ++ parent.map(_.rewriters).getOrElse(Seq()) ++ degrel.primitives.rewriter.default

  def atoms: Iterable[Vertex] = cell.roots

  def atomTargets: Iterable[RewritingTarget] = cell.roots.map { r =>
    RewritingTarget(r.asHeader, r.asHeader, this)
  }

  def rewriteTargets: Iterable[RewritingTarget] = {
    CellTraverser(this.cell, this)
  }

  override def spawn(cell: Vertex): Driver = {
    val spawningHeader = cell.asHeader
    val fut = node.spawnSomewhere(cell, this.binding, VertexPin(spawningHeader.id, 0), this)
    Await.result(fut, Timeouts.short.duration) match {
      case Right(drv: Driver) => {
        this.children += drv.id -> drv
        drv
      }
      case Left(msg: Throwable) => throw msg
    }
  }

  def cleanup(): Unit = {
    if (this.isValid && this.isActive) {
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
        print(Console.GREEN)
        println("--- Apply ---")
        println(rw.pp)
        println(res)
        print(Console.BLUE)
        println("--- Result ---")
        println(this.header.pp)
        println(Console.RESET)
      }
    }
    res.done
  }

  def addContinueRewriter(rw: ContinueRewriter) = {
    contRewriters += rw
  }

  override def writeVertex(target: VertexHeader, value: Vertex): Unit = {
    println(s"WRITE(on: $id) $target(${target.id}) <= ${value.pp} ")
    if (target.id == this.header.id) {
      state = DriverState.Finished(this.returnTo, value)
      this.parent match {
        case Some(drv: LocalDriver) => {
          // fin!
          drv.writeVertex(target, value)
        }
        case Some(drv: RemoteDriver) => {
          // fin!
          drv.writeTo(returnTo.id, value)
        }
        case _ => {
          this.header.write(value)
        }
      }
    } else {
      val tryOwn = GraphVisitor(new TransferOwnerVisitor(this.header))
      tryOwn.visit(value)
      target.write(value)
    }
  }

  override def removeRoot(v: Vertex): Unit = {
    this.cell.removeRoot(v)
  }

  override def dispatch(target: VertexHeader, value: Vertex)(implicit ec: ExecutionContext): Future[Unit] = {
    if (target.id == this.header.id) {
      val transfer = GraphVisitor(Traverser.cell _, new TransferOwnerVisitor(this.header))
      transfer.visit(value)
      if (value.isCell) {
        this.spawn(value.asCell)
      }
      this.cell.addRoot(value)
      Future {}
    } else if (target.id.hasSameOwner(this.id)) async {
      if (target.isCell) {
        target.asCell.addRoot(value)
      }
    } else async {
      await(node.lookupOwner(target.id)) match {
        case Right(drv) => {
          await(drv.dispatch(target, value))
        }
        case Left(th) => {
          logger.warn(s"Ignoreing Ownership! at $id != ${target.id}")
          if (target.isCell) {
            target.asCell.addRoot(value)
          } else {
            logger.error("Cannot add vertex to non-cell vertex")
            throw new RuntimeException("Cannot add vertex to non-cell vertex")
          }
        }
      }
    }
  }

  override def binding: Binding = {
    if (this.header.isCell) {
      this.cell.binding
    } else {
      Binding.empty()
    }
  }

  override def getVertex(id: ID): Option[Vertex] = {
    //Traverser(this.header, TraverserCutOff.cell(this.header)).find(_.id == id)
    Traverser(this.header).find(_.id == id)
  }

  override def onChildStateUpdated(childReturnTo: VertexPin, id: ID, state: DriverState): Unit = {
    if (this.isPaused && this.children.values.forall(_.isStopped)) {
      this.state = DriverState.Stopped()
    }
  }

  override def toString: String = {
    s"<LocalDriver $id on: ${node.selfID} state: $state parent: ${parent.map(_.id)}>"
  }
}

object LocalDriver {
  def apply(): LocalDriver = {
    LocalDriver(Cell())
  }

  def apply(cell: Cell, chassis: Chassis = null): LocalDriver = {
    val chas = if (chassis == null) Chassis.create() else chassis
    val node = LocalNode()
    new RootLocalDriver(cell.asHeader, chas, node)
  }
}

class RootLocalDriver(_header: VertexHeader, _chassis: Chassis, _node: LocalNode) extends LocalDriver(_header, _chassis, _node, _header.pin, None) {
}
