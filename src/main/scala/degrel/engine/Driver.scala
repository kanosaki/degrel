package degrel.engine
import degrel.core.{Cell, CellBody, Vertex}
import degrel.engine.rewriting.{Binding, Rewriter, RewritingTarget}
import degrel.engine.sphere.Sphere

trait Driver {
  def isActive: Boolean

  def resource: Sphere

  def stepUntilStop(limit: Int = -1): Int

  def rewriters: Seq[Rewriter]

  def cell: CellBody

  def spawn(cell: Vertex): Vertex

  def writeVertex(target: RewritingTarget, value: Vertex): Unit

  def removeRoot(v: Vertex): Unit

  def addRoot(target: Cell, value: Vertex)

  def binding: Binding
}
