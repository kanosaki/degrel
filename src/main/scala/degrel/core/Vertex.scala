package degrel.core

trait Vertex {
  def edges(label: Label = Label.wildcard): Iterable[Edge]
  def label : Label

  def id: ID = {
    this.localID
  }

  protected def localID: ID = {
    LocalID(System.identityHashCode(this))
  }
}

object Vertex {
  def apply(label: String, edges: Iterable[Edge]): Vertex = {
    val body = VertexBody(Label(label), edges.toSeq)
    new VertexHeader(body)
  }
}

