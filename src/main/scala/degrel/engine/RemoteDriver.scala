package degrel.engine

import akka.actor.ActorRef
import degrel.core.{Cell, Vertex}
import degrel.engine.rewriting.{Binding, Rewriter, RewritingTarget}
import degrel.engine.sphere.Sphere

class RemoteDriver extends Driver {
  // Contents: DriverContainer
  private val ref: ActorRef = ???

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
}
