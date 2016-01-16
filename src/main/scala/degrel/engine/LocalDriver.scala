package degrel.engine

import degrel.DegrelException
import degrel.cluster.SpawnResult.{NoVacantNode, OtherError}
import degrel.cluster.{LocalNode, SpawnResultSuccess, Timeouts}
import degrel.core.DriverState.{Stopped, Finished}
import degrel.core._
import degrel.core.transformer.{FixIDVisitor, GraphVisitor}
import degrel.engine.rewriting._
import degrel.engine.sphere.Sphere
import degrel.utils.PrettyPrintOptions

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future, stm}

/**
  * Cellの実行をします
  */
class LocalDriver(val header: VertexHeader,
                  val chassis: Chassis,
                  val node: LocalNode,
                  val returnTo: VertexPin,
                  val parent: Option[Driver])
                 (implicit val executionContext: ExecutionContext) extends Reactor with Driver {
  implicit val printOption = PrettyPrintOptions(multiLine = true, showAllId = true)
  private var children = new mutable.HashMap[ID, Driver]()
  private val spawnInfo = mutable.HashMap[ID, SpawnInfo]()
  private var contRewriters: mutable.Buffer[ContinueRewriter] = mutable.ListBuffer()
  private val _spawnInProgressCount = stm.Ref(0)
  private val spawnQueue = mutable.ListBuffer[Vertex]()

  def pendingSpawns: Int = _spawnInProgressCount.single.get

  val idSpace = node.nextIDSpace()
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
      println(s"DRIVER_START: $id(on: ${node.selfID})>>>>>")
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
        println(s"ID: $id(on: ${node.selfID}) steps: $count")
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
      this.prepare()
      val rewrote = this.stepRecursive()
      this.cleanup()
      count += 1
      if (!rewrote) { // todo: rewrote effected by child result
        state = this.state match {
          case _ if !this.hasActiveChild && !this.preventStop => DriverState.Stopped()
          case _ => DriverState.Stopping()
        }
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

  override def spawn(cell: Vertex): Option[Driver] = {
    logger.debug(s"SPAWNING ${cell.pp}")
    val spawningHeader = cell.asHeader
    val originID = spawningHeader.id
    _spawnInProgressCount.single.transform(_ + 1)
    this.spawnInfo += originID -> SpawnInfo(spawningHeader, originID)
    val fut = node.spawnSomewhere(cell, this.binding, VertexPin(spawningHeader.id, 0), this)
    val res = Await.result(fut, Timeouts.short.duration)
    _spawnInProgressCount.single.transform(_ - 1)
    res match {
      case res: SpawnResultSuccess => {
        val drv = res.result
        this.spawnInfo.get(originID) match {
          case Some(si) => si.spawnID = Some(drv.id)
          case None => {
            logger.warn(s"Cannot update SpawnInfo (maybe too short life cell?) ID:${originID}")
          }
        }
        this.children += drv.id -> drv
        Some(drv)
      }
      case NoVacantNode() => {
        spawnQueue += cell
        None
      }
      case OtherError(th) => throw th
    }
  }

  def cleanup(): Unit = {
    if (this.isValid && this.isActive) {
      this.children = this.children.filter(_._2.isActive)
      this.cell.roots.filter(v => {
        v.isCell && v.edges.isEmpty
      }).foreach {
        v =>
          this.removeRoot(v)
      }
    }
  }

  private def execRewrite(rw: Rewriter, rc: RewritingTarget): Boolean = {
    val res = chassis.diagnostics.rewriteSpan.enter {
      rw.rewrite(rc)
    }

    if (res.done) {
      if (chassis.verbose) {
        print(Console.GREEN)
        println(s"Step Result: ID: $id(on: ${
          node.selfID
        })")
        println("--- Apply ---")
        println(rw.pp)
        println(res)
        println(Console.RESET)
      }
      chassis.diagnostics.applySpan.enter {
        res.exec(this)
      }
      if (chassis.verbose) {
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
    logger.debug(s"WRITE(on: $id) $target(${
      target.id
    }) <= ${
      value.pp
    } ")
    if (target.id == this.header.id) {
      state = DriverState.Finished(this.returnTo, value)
    } else {
      val tryOwn = GraphVisitor(new FixIDVisitor(this.idSpace))
      tryOwn.visit(value)
      target.write(value)
    }
  }

  override def removeRoot(v: Vertex): Unit = {
    this.cell.removeRoot(v)
  }

  override def dispatch(target: VertexHeader, value: Vertex): Future[Unit] = {
    if (target.id == this.header.id) {
      val transfer = GraphVisitor(new FixIDVisitor(this.idSpace))
      transfer.visit(value)
      if (value.isCell) {
        this.spawn(value.asCell)
      }
      this.cell.addRoot(value)
      Future {
      }
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
          logger.warn(s"Ignoreing Ownership! at $id != ${
            target.id
          }")
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
    state match {
      case Finished(retTo, result) => {
        this.spawnInfo.get(retTo.id) match {
          case Some(sInfo) => {
            sInfo.header.updateID(sInfo.originID)
            sInfo.header.write(result)
            spawnInfo -= retTo.id
            // re-check
            this.start()
          }
          case None => {
            println(s"$retTo ${
              this.id
            }")
            println(this.header.pp(PrettyPrintOptions(showAllId = true, multiLine = true)))
            None.get
          }
        }
      }
      case Stopped() => {
        this.start()
      }
      case _ =>
    }
    logger.debug(s"CHILD_STATE_UPDATE: $childReturnTo $id $state on ${
      this.id
    } ${
      this.children.get(id)
    }")
  }

  override def toString: String = {
    s"<LocalDriver $id on: ${
      node.selfID
    } state: $state parent: ${
      parent.map(_.id)
    }>"
  }

  def hasActiveChild: Boolean = children.values.exists(!_.state.isStopped) || spawnQueue.nonEmpty
}

object LocalDriver {
  def apply()(implicit ec: ExecutionContext): LocalDriver = {
    LocalDriver(Cell())
  }

  def apply(cell: Cell, chassis: Chassis = null)(implicit ec: ExecutionContext): LocalDriver = {
    val chas = if (chassis == null) Chassis.create() else chassis
    val node = LocalNode()
    new RootLocalDriver(cell.asHeader, chas, node)
  }
}

class RootLocalDriver(_header: VertexHeader, _chassis: Chassis, _node: LocalNode)(implicit executionContext: ExecutionContext) extends LocalDriver(_header, _chassis, _node, _header.pin, None) {
}

case class SpawnInfo(header: VertexHeader, originID: ID) {
  var spawnID: Option[ID] = None
}

