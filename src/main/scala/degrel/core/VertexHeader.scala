package degrel.core

import degrel.rewriting.BuildingContext
import degrel.utils.support._


class VertexHeader(f: => VertexBody) extends Vertex {
  private var _body: VertexBody = null

  def body: VertexBody = {
    if (_body == null) {
      _body = f
    }
    _body
  }

  def edges(label: Label): Iterable[Edge] = body.edges(label)

  def groupedEdges: Iterable[Iterable[Edge]] = body.groupedEdges

  def label: Label = body.label

  def repr: String = {
    val id = this.hashCode % 1000
    s"<${body.repr}@${id.hex()}>"
  }

  def reprRecursive(trajectory: Trajectory) = {
    val id = this.hashCode % 1000
    trajectory.walk(this) {
      case Right(nextHistory) => {
        s"<${body.reprRecursive(nextHistory)}@${id.hex()}>"
      }
      case Left(_) => {
        s"<${body.repr}@${id.hex()}>"
      }
    }
  }

  def freezeRecursive(footprints: Footprints[Vertex]): Vertex = {
    footprints.stamp(this) {
      case Right(fp) => {
        this.body.freezeRecursive(fp)
      }
      case Left(fp) => fp.resultOf(this)
    }
  }

  def copyRecursive(footprints: Footprints[Vertex]): Vertex = {
    footprints.stamp(this) {
      case Right(fp) => {
        new VertexHeader(this.body.copyRecursive(fp).asInstanceOf[VertexBody])
      }
      case Left(fp) => fp.resultOf(this)
    }
  }

  def shallowCopy: Vertex = {
    new VertexHeader(this.body)
  }

  def isSameElement(other: Element): Boolean = other match {
    case vh: VertexHeader => this.body ==~ vh.body
    case _ => false
  }

  def attr(key: String): Option[String] = body.attr(key)

  def build(context: BuildingContext): Vertex = body.build(context)

  def write(v: Vertex) = v match {
    case vb: VertexBody => _body = vb
    case vh: VertexHeader => _body = vh.body
  }

  def attributes: Map[String, String] = body.attributes
}
