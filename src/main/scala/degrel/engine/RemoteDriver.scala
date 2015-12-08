package degrel.engine

import akka.actor.ActorRef
import akka.pattern.ask
import degrel.cluster.messages._
import degrel.cluster.{DGraph, LocalNode, QueryOption, Timeouts}
import degrel.core.DriverState.Finished
import degrel.core._
import degrel.engine.rewriting.{Binding, Rewriter}
import degrel.engine.sphere.Sphere

import scala.async.Async.{async, await}
import scala.concurrent.{Await, ExecutionContext, Future}

// note: remoteNode is not a single cell(or a driver) wrapper, it might contains several cells(also drivers)
class RemoteDriver(override val header: VertexHeader, remoteNode: ActorRef, node: LocalNode, val binding: Binding, val returnTo: VertexPin, val parentPin: Option[VertexPin])(implicit ec: ExecutionContext) extends Driver {
  implicit val timeout = Timeouts.short
  // すぐにlookupすると，まだ登録されていないためエラーになる
  private lazy val _parent = async {
    this.parentPin match {
      case Some(pPin) => {
        await(node.lookupOwner(pPin.id)) match {
          case Right(drv) => Some(drv)
          case Left(msg) => throw msg
        }
      }
      case None => {
        None
      }
    }
  }

  override def dispatch(target: VertexHeader, value: Vertex)(implicit ec: ExecutionContext): Future[Unit] = {
    val dGraph = node.exchanger.packAll(value, move = true)
    remoteNode ! SendGraph(target.id, dGraph)
    Future {}
  }

  override def spawn(cell: Vertex): Driver = {
    ???
  }

  override def writeVertex(target: VertexHeader, value: Vertex): Unit = {
    logger.info(s"WRITE(to Remote($returnTo))")
    val dGraph = node.exchanger.packAll(value)
    remoteNode ! WriteVertex(target.id, dGraph)
  }

  def writeTo(targetID: ID, value: Vertex): Unit = {
    logger.info(s"WRITE(to RemoteID($returnTo)) value: ${value.pp}")
    val dGraph = node.exchanger.packAll(value)
    remoteNode ! WriteVertex(targetID, dGraph)
  }

  override def removeRoot(v: Vertex): Unit = {
    remoteNode ! RemoveRoot(v.id)
  }

  override def stepUntilStop(limit: Int): Int = 0

  override def resource: Sphere = node.getSphere(this)

  override def getVertex(id: ID): Option[Vertex] = {
    val fut = async {
      await(remoteNode ? QueryGraph(id, QueryOption.WholeCell)) match {
        case Right(dGraph: DGraph) => {
          Some(node.exchanger.unpack(dGraph))
        }
        case Left(msg: Throwable) => {
          println(s"Failed to fetch graph for $id: $msg")
          if (msg != null) {
            msg.printStackTrace()
          }
          None
        }
        case others => {
          println(s"Cannot fetch graph for $id: $others")
          None
        }
      }
    }
    Await.result(fut, Timeouts.short.duration)
  }

  override def stepRecursive(): Boolean = false

  override def rewriters: Seq[Rewriter] = selfRewriters ++ baseRewriters ++ parent.map(_.rewriters).getOrElse(Seq())

  override def parent: Option[Driver] = Await.result(_parent, Timeouts.short.duration)

  // receive from remote driver
  def remoteUpdated(info: DriverInfo) = {
    info.state match {
      case Finished(pin, value) => {
        if (pin.id == this.id) {
          this.header.write(value)
        }
      }
    }
    this.state = info.state
  }

  // proxy to remote driver
  override def onChildStateUpdated(childReturnTo: VertexPin, childID: ID, childState: DriverState): Unit = {
    remoteNode ! TellDriverInfo(DriverInfo(childReturnTo, childID, childState))
  }

  override def toString: String = {
    s"<RemoteDriver ID: $id on: ${node.selfID} state: $state parent: ${parent.map(_.id)}|${header.pp}>"
  }
}

object RemoteDriver {
  def remoteDriver(info: DriverParameter, node: LocalNode)(implicit ec: ExecutionContext): RemoteDriver = {
    localPhantom(RemoteVertexHeader(info.root, node), info, node)
  }

  def localPhantom(root: VertexHeader, info: DriverParameter, node: LocalNode)(implicit ec: ExecutionContext): RemoteDriver = {
    localPhantom(root, info.hostedOn, node, info.binding, info.returnTo, info.parentPin)
  }

  def localPhantom(root: VertexHeader, hostedOn: ActorRef, node: LocalNode, binding: Binding, returnTo: VertexPin, parentPin: Option[VertexPin])(implicit ec: ExecutionContext): RemoteDriver = {
    new RemoteDriver(root, hostedOn, node, binding, returnTo, parentPin)
  }

}
