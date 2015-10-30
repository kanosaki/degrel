package degrel.engine

import degrel.core.{Cell, CellBody, Vertex}
import degrel.engine.rewriting.{Binding, Rewriter, RewritingTarget}
import degrel.engine.sphere.Sphere

class RemoteDriver extends Driver {
  override def addRoot(target: Cell, value: Vertex): Unit = ???

  override def spawn(cell: Vertex): Vertex = ???

  override def isActive: Boolean = ???

  override def writeVertex(target: RewritingTarget, value: Vertex): Unit = ???

  override def removeRoot(v: Vertex): Unit = ???

  override def stepUntilStop(limit: Int): Int = ???

  override def cell: CellBody = ???

  override def binding: Binding = ???

  override def rewriters: Seq[Rewriter] = ???

  override def resource: Sphere = ???
}
