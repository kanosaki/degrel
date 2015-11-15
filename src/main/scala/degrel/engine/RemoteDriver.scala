package degrel.engine

import akka.actor.ActorRef
import degrel.cluster.LocalNode
import degrel.core.{ID, Cell, Vertex}
import degrel.engine.rewriting.{Binding, Rewriter, RewritingTarget}
import degrel.engine.sphere.Sphere

class RemoteDriver(ref: ActorRef, node: LocalNode) extends Driver {

  override def dispatchRoot(target: Cell, value: Vertex): Unit = ???

  override def spawn(cell: Vertex): Driver = ???

  override def isActive: Boolean = ???

  override def writeVertex(target: RewritingTarget, value: Vertex): Unit = ???

  override def removeRoot(v: Vertex): Unit = ???

  override def stepUntilStop(limit: Int): Int = ???

  override def binding: Binding = ???

  override def rewriters: Seq[Rewriter] = ???

  override def resource: Sphere = ???

  override def header: Vertex = ???

  override def addRoot(value: Vertex): Unit = ???

  override def getVertex(id: ID): Option[Vertex] = ???
}

object RemoteDriver{
  def apply(ref: ActorRef, node: LocalNode) = {
    new RemoteDriver(ref, node)
  }
}
