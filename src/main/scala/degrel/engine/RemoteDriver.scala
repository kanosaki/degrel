package degrel.engine

import akka.actor.ActorRef
import akka.pattern.ask
import degrel.cluster.messages._
import degrel.cluster.{LocalNode, Timeouts}
import degrel.core.{Cell, ID, Vertex}
import degrel.engine.rewriting.{Binding, Rewriter, RewritingTarget}
import degrel.engine.sphere.Sphere

import scala.concurrent.{ExecutionContext, Await}

// note: remoteNode is not a single cell(or a driver) wrapper, it might contains several cells(also drivers)
class RemoteDriver(remoteNode: ActorRef, node: LocalNode)(implicit ec: ExecutionContext) extends Driver {
  implicit val timeout = Timeouts.short

  override def dispatch(target: Cell, value: Vertex): Unit = {
    val dGraph = node.exchanger.packAll(value, move = true)
    remoteNode ! SendGraph(target.id, dGraph)
  }

  override def spawn(cell: Vertex): Driver = {
    node.spawn(cell)
  }

  override def isActive: Boolean = {
    // todo: should be future?
    val fut = (remoteNode ? QueryStatus()).mapTo[DriverState].map(_.isActive)
    Await.result(fut, Timeouts.short.duration)
  }

  override def writeVertex(target: RewritingTarget, value: Vertex): Unit = {
    val dGraph = node.exchanger.pack(value)
    remoteNode ! WriteVertex(target.target.id, dGraph)
  }

  override def removeRoot(v: Vertex): Unit = {
    remoteNode ! RemoveRoot(v.id)
  }

  override def stepUntilStop(limit: Int): Int = 0

  override def binding: Binding = ???

  override val resource: Sphere = node.getSphere(this)

  override val header: Vertex = {
    Await.result((remoteNode ? QueryHeader()).mapTo[Vertex], Timeouts.short.duration)
  }

  override def getVertex(id: ID): Option[Vertex] = ???

  override def stepRecursive(): Boolean = false

  override def rewriters: Seq[Rewriter] = ???
}

object RemoteDriver {
  def apply(ref: ActorRef, node: LocalNode)(implicit ec: ExecutionContext) = {
    new RemoteDriver(ref, node)
  }
}
