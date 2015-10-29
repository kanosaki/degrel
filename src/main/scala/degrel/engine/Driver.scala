package degrel.engine
import degrel.core.{Cell, CellBody, Vertex}
import degrel.engine.rewriting.{ContinueRewriter, Binding, RewritingTarget, Rewriter}
import degrel.engine.sphere.Sphere

trait Driver {
  def chassis: Chassis

  var rewritee: RewriteeSet

  def isActive: Boolean

  val resource: Sphere

  def stepUntilStop(limit: Int = -1): Int

  def rewriters: Seq[Rewriter]

  def atoms: Iterable[Vertex]

  def atomTargets: Iterable[RewritingTarget] = cell.roots.map { r =>
    RewritingTarget(r.asHeader, r.asHeader, this)
  }

  def addContinueRewriter(rw: ContinueRewriter): Unit

  def cell: CellBody

  def spawn(cell: Vertex): Vertex

  def writeVertex(target: RewritingTarget, value: Vertex): Unit

  def removeRoot(v: Vertex): Unit

  def addRoot(target: Cell, value: Vertex)

  def binding: Binding
}
