package degrel.core

trait VertexHeader extends Vertex {
  def body: VertexBody

  def write(v: Vertex): Unit

  def edges: Iterable[Edge] = body.edges

  def label: Label = body.label

  def attr(key: Label): Option[String] = body.attr(key)

  def attributes: Map[Label, String] = body.attributes


  override def isValue[T]: Boolean = body.isValue[T]

  override def getValue[T]: Option[T] = body.getValue[T]

  val id: ID = ID.autoAssign
}

object VertexHeader {
  def apply(body: VertexBody): VertexHeader = {
    new LocalVertexHeader(body)
  }
}
