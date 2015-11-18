package degrel.engine

import degrel.core.{Cell, CellBody, ID, Vertex}
import degrel.engine.rewriting.{Binding, Rewriter, RewritingTarget}
import degrel.engine.sphere.Sphere

trait Driver {
  def isActive: Boolean

  def resource: Sphere

  def stepUntilStop(limit: Int = -1): Int

  def cell: CellBody = this.header.unhead[CellBody]

  def header: Vertex

  def spawn(cell: Vertex): Driver

  def writeVertex(target: RewritingTarget, value: Vertex): Unit

  def removeRoot(v: Vertex): Unit

  def dispatchRoot(target: Cell, value: Vertex)

  def addRoot(value: Vertex)

  def binding: Binding

  def getVertex(id: ID): Option[Vertex]

  def stepRecursive(): Boolean

  def rewriters: Seq[Rewriter]

  /**
    * Send message vertex underlying cell
    */
  def send(msg: Vertex) = {
    this.dispatchRoot(this.cell, msg)
  }

  def baseRewriters: Seq[Rewriter] = cell.bases.flatMap(_.rules.map(Rewriter(_)))

  def selfRewriters: Seq[Rewriter] = cell.rules.map(Rewriter(_))

}
