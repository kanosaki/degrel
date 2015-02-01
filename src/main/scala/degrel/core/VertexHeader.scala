package degrel.core

import degrel.engine.rewriting.BuildingContext

import scala.concurrent.stm


class VertexHeader(f: VertexBody) extends Vertex {
  private[this] val _locator = stm.Ref(VertexLocator.createNew(f))

  def edges(label: Label): Iterable[Edge] = body.edges(label)

  def groupedEdges: Iterable[Iterable[Edge]] = body.groupedEdges

  def body: VertexBody = {
    this.locator.single.get.activeVertex
  }

  protected def locator: stm.Ref[VertexLocator] = {
    _locator
  }

  def label: Label = body.label

  def repr: String = {
    s"<${body.repr}>"
  }

  def reprRecursive(trajectory: Trajectory) = {
    trajectory.walk(this) {
      case Unvisited(nextHistory) => {
        s"<${body.reprRecursive(nextHistory)}>"
      }
      case Visited(_) => {
        s"<${body.repr}(..)>"
      }
    }
  }

  def shallowCopy: Vertex = {
    new VertexHeader(this.body)
  }

  def isSameElement(other: Element): Boolean = other match {
    case vh: VertexHeader => this.body ==~ vh.body
    case _ => false
  }

  def attr(key: Label): Option[String] = body.attr(key)

  def build(context: BuildingContext): Vertex = body.build(context)


  def write(v: Vertex) = v match {
    case vb: VertexBody => locator.single.set(VertexLocator.createNew(vb))
    case vh: VertexHeader => locator.single.set(VertexLocator.createNew(vh.body))
  }

  def beginTransaction()(implicit txn: Transaction): (VertexLocator, VertexLocator) = {
    stm.atomic {
      implicit _txn =>
        val loc = this.locator.single.get
        (loc, VertexLocator.createFrom(loc))
    }
  }

  def commitTransaction(prev: VertexLocator, created: VertexLocator)(implicit txn: Transaction): Boolean = {
    _locator.single.compareAndSetIdentity(prev, created)
  }

  def attributes: Map[Label, String] = body.attributes

  def id: ID = body.id
}
