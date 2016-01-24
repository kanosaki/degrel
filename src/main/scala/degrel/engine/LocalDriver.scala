package degrel.engine


import java.util.concurrent.PriorityBlockingQueue
import java.util.function.Predicate

import degrel.DegrelException
import degrel.cluster.SpawnResult.{NoVacantNode, OtherError, RemoteSpawned}
import degrel.cluster.{LocalNode, SpawnResultSuccess}
import degrel.core.DriverState._
import degrel.core._
import degrel.core.transformer.{FixIDVisitor, GraphVisitor}
import degrel.engine.rewriting._
import degrel.engine.sphere.Sphere
import degrel.utils.PrettyPrintOptions

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent._

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
  private val opQueue = new PriorityBlockingQueue[DriverOp[_]]()
  private var activeThread: Future[Unit] = null
  private val __startLock = new AnyRef()

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

  override def stepUntilStop(limit: Int = -1): Future[Long] = async {
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
    if (await(this.step())) {
      this.schedule(Scan())
    }
    count += 1
    if (limit > 0 && count > limit) {
      throw DegrelException(s"Exec limitation exceeded. \n${cell.pp}")
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

  def checkSpawn(): Boolean = {
    var found = false
    atoms.foreach(r => {
      if (r.isCell && !r.isNil) {
        if (!children.contains(r.id)) {
          this.spawn(r)
          found = true
        }
      } else {
        r.thru(0).filter(v => v.isCell && !v.isNil).foreach { c =>
          if (!children.contains(c.id)) {
            this.spawn(c)
            found = true
          }
        }
      }
    })
    found
  }

  def prepare(): Unit = {
    this.checkSpawn()
  }

  /**
    * 1回書き換えます
    *
    * 1. 探索を実行する規則の選択
    * 2. 探索の実行
    * 3. 書き換えの実行
    */
  def step(): Future[Boolean] = async {
    val rewrote = this.atoms.exists { atom =>
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
    if (!rewrote) {
      if (this.preventStop) {
        state = DriverState.Paused()
      } else {
        val allChildrenAreStopping = this.children.values.forall(_.state.isStopping)
        state = (this.hasActiveChild, parent.isDefined) match {
          case (false, false) => DriverState.Stopped() // no parent, no children
          case (false, true) => DriverState.Stopping() // no children but has parent
          case (true, false) if allChildrenAreStopping => DriverState.Stopped()
          case (true, true) if allChildrenAreStopping => DriverState.Stopping()
          case _ => DriverState.Paused()
        }
      }
    }
    rewrote
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

  override def spawn(cell: Vertex): Unit = {
    if (cell.isCell && !cell.isNil) {
      this.schedule(Spawn(cell))
    }
  }

  override def writeVertex(target: VertexHeader, value: Vertex): Unit = {
    this.schedule(Write(target, value))
  }

  override def removeRoot(v: Vertex): Unit = {
    this.schedule(RemoveRoot(v))
  }

  override def dispatch(target: VertexHeader, value: Vertex): Future[Unit] = {
    val op = AddRoot(target, value)
    this.schedule(op)
    op.done.future
  }

  protected def schedule[T](op: DriverOp[T],
                            intercept: Boolean = false,
                            startOperate: Boolean = true): Future[T] = {
    if (op.isInstanceOf[Scan]) {
      opQueue.removeIf(new Predicate[DriverOp[_]] {
        override def test(t: DriverOp[_]): Boolean = {
          t.isInstanceOf[Scan]
        }
      })
    }
    if (intercept) {
      opQueue.add(InterceptOp(op))
    } else {
      opQueue.add(op)
    }
    //logger.debug(s"SCHED@$id: $op intercept: $intercept start: $startOperate queue: $opQueue")
    if (startOperate) {
      this.operate()
    }
    op.done.future
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
            logger.debug(s"CHILD_STATE_UPDATE(Finished): $childReturnTo $id $state on ${this.id} ${this.children.get(id)}")
            sInfo.header.updateID(sInfo.originID)
            sInfo.header.write(result)
            spawnInfo -= retTo.id
            // re-check
            this.state = Active()
            schedule(Scan())
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
        logger.debug(s"CHILD_STATE_UPDATE(Stopped): $childReturnTo $id $state on ${this.id} ${this.children.get(id)}")
        schedule(Scan())
      }
      case Stopping() => {
        logger.debug(s"CHILD_STATE_UPDATE(Stopping): $childReturnTo $id $state on ${this.id} ${this.children.get(id)}")
        schedule(Scan())
      }
      case other => {
        logger.debug(s"CHILD_STATE_UPDATE($other): $childReturnTo $id $state on ${this.id} ${this.children.get(id)}")
      }
    }
  }

  override def toString: String = {
    s"<LocalDriver $id on: ${
      node.selfID
    } state: $state parent: ${
      parent.map(_.id)
    }>"
  }

  private def consumeQueue(): Future[Unit] = async {
    // cannot use inner function to utilize async
    //logger.debug(s"CONSUMING@$id state: $state $opQueue")
    var nextOp = opQueue.poll()
    while (nextOp != null && !this.isStopped && !this.state.isInstanceOf[Intercepted]) {
      //logger.debug(s"OP@$id: $nextOp -- $opQueue")
      await(nextOp.exec())
      nextOp = opQueue.poll()
    }
    __startLock.synchronized {
      // re-check queue with lock and finish
      if (!opQueue.isEmpty && !this.isStopped && !this.state.isInstanceOf[Intercepted]) {
        this.consumeQueue()
      } else {
        activeThread = null
      }
    }
  }

  def operate(): Future[Unit] = {
    __startLock.synchronized {
      if (activeThread == null) {
        activeThread = async {
          await(this.consumeQueue())
        }
      }
      activeThread
    }
  }

  override def start(): Future[Vertex] = {
    this.schedule(Scan())
    this.finValue.future
  }

  def hasActiveChild: Boolean = children.values.exists(_.state.isActive)

  protected trait DriverOp[T] extends Comparable[DriverOp[_]] {
    type OpResult = Unit

    def exec(): Future[OpResult]

    def self: LocalDriver = LocalDriver.this

    val done: Promise[T] = Promise()


    override def compareTo(o: DriverOp[_]): Int = {
      Integer.compare(this.priority, o.priority)
    }

    def priority: Int = 0
  }

  case class Write(target: VertexHeader, value: Vertex) extends DriverOp[Unit] {
    override def exec(): Future[OpResult] = async {
      logger.debug(s"WRITE(on: $id) $target(${
        target.id
      }) <= ${
        value.pp
      } ")
      if (target.id == self.header.id) {
        self.state = DriverState.Finished(self.returnTo, value)
      } else {
        val tryOwn = GraphVisitor(new FixIDVisitor(self.idSpace))
        tryOwn.visit(value)
        target.write(value)
        schedule(Scan())
      }
      this.done.success(())
    }
  }

  case class Scan() extends DriverOp[Unit] {
    override def exec(): Future[OpResult] = async {
      if (!self.isStopped) {
        if (checkSpawn()) {
          schedule(Scan())
        } else {
          state = Active()
          await(self.stepUntilStop())
        }
      }
      this.done.success(())
    }

    // low priority
    override def priority: Int = 100
  }

  case class AddRoot(target: VertexHeader, value: Vertex) extends DriverOp[Unit] {
    override def exec(): Future[OpResult] = async {
      println("ADD_ROOT")
      if (target.id == self.header.id) {
        println("ADD_ROOT to me")
        val transfer = GraphVisitor(new FixIDVisitor(self.idSpace))
        transfer.visit(value)
        self.cell.addRoot(value)
        if (value.isCell) {
          self.spawn(value.asCell)
        }
        state = Active()
        schedule(Scan())
      } else if (target.id.hasSameOwner(self.id)) {
        println("ADD_ROOT to vertex own by me")
        spawnInfo.get(target.id) match {
          case Some(si) => {
            si.driver.get.dispatch(target, value)
          }
          case None => {
            if (target.isCell) {
              target.asCell.addRoot(value)
            }
          }
        }
        state = Active()
        schedule(Scan())
      } else {
        println("ADD_ROOT Another")
        await(node.lookupOwner(target.id)) match {
          case Right(drv) => {
            drv.dispatch(target, value)
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
      this.done.success(())
    }
  }

  case class RemoveRoot(v: Vertex) extends DriverOp[Unit] {
    override def exec(): Future[OpResult] = async {
      self.cell.removeRoot(v)
      this.done.success(())
    }
  }

  case class Spawn(cell: Vertex) extends DriverOp[Option[Driver]] {
    override def exec(): Future[OpResult] = async {
      assert(cell != self.header)
      logger.debug(s"SPAWNING ${cell.pp}")
      val spawningHeader = cell.asHeader
      val originID = spawningHeader.id
      self.spawnInfo += originID -> SpawnInfo(spawningHeader, spawningHeader.body, originID)
      val res = await(self.node.spawnSomewhere(cell, self.binding, VertexPin(spawningHeader.id, 0), self))
      res match {
        case res: SpawnResultSuccess => {
          val isRemote = res.isInstanceOf[RemoteSpawned]
          if (isRemote) {
            spawningHeader.write(Vertex("__remote__", Seq(), Map()))
          }
          val drv = res.result
          self.spawnInfo.get(originID) match {
            case Some(si) => {
              si.isRemote = isRemote
              si.driver = Some(drv)
            }
            case None => {
              logger.warn(s"Cannot update SpawnInfo (maybe it was a too short life cell?) ID:${originID}")
            }
          }
          self.children += drv.id -> drv
          drv.start()
          this.done.success(Some(drv))
        }
        case NoVacantNode() => {
          self.state = Intercepted()
          logger.info(s"Execution paused due to worker full! on $id")
          schedule(Spawn(cell), intercept = true, startOperate = false)
          this.done.success(None)
        }
        case OtherError(th) => throw th
      }
    }
  }

  case class InterceptOp[CT](op: DriverOp[CT]) extends DriverOp[CT] {
    override def exec(): Future[OpResult] = async {
      await(op.exec())
    }

    override def priority: Int = -100
  }

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

case class SpawnInfo(header: VertexHeader, prevBody: VertexBody, originID: ID) {
  var isRemote = false
  var driver: Option[Driver] = None

  def spawnID: Option[ID] = driver.map(_.id)
}

