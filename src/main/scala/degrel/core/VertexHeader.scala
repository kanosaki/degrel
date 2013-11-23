package degrel.core

class VertexHeader(protected var body: VertexBody) extends Vertex {

  override def equals(other: Any) = other match {
    case vh: VertexHeader => vh.body == this.body
    case vb: VertexBody => vb == this.body
    case _ => false
  }

  def edges(label: Label): Iterable[Edge] = body.edges(label)

  def label : Label = body.label

  override def toString: String = {
    s"<$id#$body>"
  }

}
