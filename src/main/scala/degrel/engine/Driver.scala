package degrel.engine

import akka.actor.ActorRef
import degrel.Logger
import degrel.cluster.journal.Journal
import degrel.cluster.messages.{DriverInfo, DriverParameter}
import degrel.cluster.{DBinding, DDriverState, LocalNode}
import degrel.core._
import degrel.engine.rewriting.{Binding, Rewriter}
import degrel.engine.sphere.Sphere
import degrel.utils.PrettyPrintOptions

import scala.async.Async.async
import scala.concurrent.{ExecutionContext, Future, Promise}

trait Driver extends Logger {
  implicit val executionContext: ExecutionContext

  var activeThread: Future[Int] = null
  val finValue: Promise[Vertex] = Promise[Vertex]()
  var preventStop = false

  protected var stateVar: DriverState = DriverState.Active()

  def state: DriverState = stateVar

  def state_=(state: DriverState): Unit = {
    require(state != null)
    if (stateVar == state || stateVar.isStopped) return
    val old = this.stateVar
    node.journal(Journal.DriverStateUpdate(this.id, DDriverState.pack(old, node, this), DDriverState.pack(state, node, this)))
    this.stateVar = state
    this.onStageChanged(old, state)
    import DriverState._
    state match {
      case Active() => this.onActive()
      case Paused(steps) => this.onPause(steps)
      case Finished(pin, v) => this.onFinished(pin, v)
      case Stopped() => this.onStopped()
      case Stopping() => this.onStopping()
      case Dead(ex) => this.onDead(ex)
    }
  }

  def onStageChanged(oldState: DriverState, newState: DriverState): Unit = {
    this.parent match {
      case Some(parentDriver) => {
        parentDriver.onChildStateUpdated(this.returnTo, this.id, newState)
      }
      case _ =>
    }
  }

  def onFinished(returnTo: VertexPin, value: Vertex): Unit = {
    //println(s"CELL_FINISH: cell: $cell ($id) value: $value")
    finValue.success(value)
  }

  def onStopping(): Unit = {
    // Re-check
    this.start()
  }

  def onStopped(): Unit = {
    this.finValue.success(this.header)
  }

  def onActive(): Unit = {}

  def onPause(steps: Long): Unit = {}

  def onDead(ex: Throwable): Unit = {}

  def isActive: Boolean = this.state match {
    case DriverState.Active() => true
    case _ => false
  }

  def node: LocalNode

  def resource: Sphere

  def stepUntilStop(limit: Int = -1): Int

  def header: VertexHeader

  def spawn(cell: Vertex): Option[Driver]

  def writeVertex(target: VertexHeader, value: Vertex): Unit

  def removeRoot(v: Vertex): Unit

  def dispatch(target: VertexHeader, value: Vertex): Future[Unit]

  def binding: Binding

  def getVertex(id: ID): Option[Vertex]

  def stepRecursive(): Boolean

  def rewriters: Seq[Rewriter]

  def cell: CellBody = this.header.unhead[CellBody]

  def onChildStateUpdated(childReturnTo: VertexPin, id: ID, state: DriverState): Unit

  /**
    * Send message vertex underlying cell
    */
  def baseRewriters: Seq[Rewriter] = cell.bases.flatMap(_.rules.map(Rewriter(_)))

  def selfRewriters: Seq[Rewriter] = cell.rules.map(Rewriter(_))

  def id: ID = this.header.id

  def parent: Option[Driver]

  def send(msg: Vertex): Future[Unit] = {
    this.dispatch(this.header, msg)
  }

  def param(whereIsHere: ActorRef): DriverParameter = {
    DriverParameter(this.id, DBinding.pack(this.binding), this.returnTo, this.parent.map(_.header.pin), whereIsHere)
  }

  def info: DriverInfo = DriverInfo(this.returnTo, this.header.id, DDriverState.pack(this.state, this.node, this))

  def returnTo: VertexPin

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

  def start(): Future[Int] = {
    // TODO: be thread safe
    val fut = if (activeThread == null) {
      async {
        try {
          if (!this.isStopped) {
            this.stepUntilStop()
          } else {
            0
          }
        } catch {
          case th: Throwable => {
            th.printStackTrace()
            throw th
          }
        }
      }
    } else {
      activeThread.map { prevCount =>
        if (!this.isStopped) {
          this.stepUntilStop() + prevCount
        } else {
          0
        }
      }
    }
    activeThread = fut
    fut
  }

}
