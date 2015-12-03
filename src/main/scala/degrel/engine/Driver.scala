package degrel.engine

import akka.actor.ActorRef
import degrel.cluster.messages.{DriverInfo, DriverParameter}
import degrel.core._
import degrel.engine.rewriting.{Binding, Rewriter}
import degrel.engine.sphere.Sphere

import scala.async.Async.async
import scala.concurrent.{ExecutionContext, Future, Promise}

trait Driver {
  var activeThread: Future[Int] = null
  val finValue: Promise[Vertex] = Promise[Vertex]()

  protected var stateVar: DriverState = DriverState.Active()

  def state: DriverState = stateVar

  def state_=(state: DriverState): Unit = {
    require(state != null)
    if (stateVar == state || stateVar.isStopped) return
    //println(s"STATE UPDATE: $id $stateVar --> $state")
    val old = this.stateVar
    this.stateVar = state
    this.onStageChanged(old, state)
    this.parent match {
      case Some(parentDriver) => {
        parentDriver.onChildStateUpdated(this.id, this.state)
      }
      case _ =>
    }
    import DriverState._
    state match {
      case Active() => this.onActive()
      case Paused(steps) => this.onPause(steps)
      case Finished(v) => this.onFinished(v)
      case Dead(ex) => this.onDead(ex)
    }
  }

  def onStageChanged(oldState: DriverState, newState: DriverState): Unit = {
    this.parent match {
      case Some(parentDriver) => {
        parentDriver.onChildStateUpdated(this.id, newState)
      }
      case _ =>
    }
  }

  def onFinished(value: Vertex): Unit = {
    //println(s"CELL_FINISH: cell: $cell ($id) value: $value")
    finValue.success(value)
  }

  def onActive(): Unit = {}

  def onPause(steps: Long): Unit = {}

  def onDead(ex: Throwable): Unit = {}

  def isActive: Boolean = this.state match {
    case DriverState.Active() => true
    case _ => false
  }

  def resource: Sphere

  def stepUntilStop(limit: Int = -1): Int

  def header: VertexHeader

  def spawn(cell: Vertex): Driver

  def writeVertex(target: VertexHeader, value: Vertex): Unit

  def removeRoot(v: Vertex): Unit

  def dispatch(target: VertexHeader, value: Vertex)(implicit ec: ExecutionContext): Future[Unit]

  def binding: Binding

  def getVertex(id: ID): Option[Vertex]

  def stepRecursive(): Boolean

  def rewriters: Seq[Rewriter]

  def cell: CellBody = this.header.unhead[CellBody]

  def onChildStateUpdated(id: ID, state: DriverState): Unit

  /**
    * Send message vertex underlying cell
    */
  def baseRewriters: Seq[Rewriter] = cell.bases.flatMap(_.rules.map(Rewriter(_)))

  def selfRewriters: Seq[Rewriter] = cell.rules.map(Rewriter(_))

  def id: ID = this.header.id

  def parent: Option[Driver]

  def send(msg: Vertex)(implicit ec: ExecutionContext): Future[Unit] = {
    this.dispatch(this.header, msg)
  }

  def param(whereIsHere: ActorRef): DriverParameter = {
    DriverParameter(this.id, this.binding, this.parent.map(_.header.pin), whereIsHere)
  }

  def info: DriverInfo = DriverInfo(this.header.id, this.state)

  /**
    * Returns true if the cell is stopped (no longer active)
    *
    * NOTE: A Cell will be stopped when
    * 1. the cell is stopped
    * 2. all children of the cell is stopped
    */
  def isStopped: Boolean = this.state.isStopped

  def isPaused: Boolean = this.state match {
    case DriverState.Paused(_) => true
    case _ => false
  }

  def start()(implicit ec: ExecutionContext): Future[Int] = {
    // TODO: be thread safe
    val fut = if (activeThread == null) {
      async {
        try {
          this.stepUntilStop()
        } catch {
          case th: Throwable => {
            th.printStackTrace()
            throw th
          }
        }
      }
    } else {
      activeThread.map { prevCount =>
        this.stepUntilStop() + prevCount
      }
    }
    activeThread = fut
    fut
  }

}
