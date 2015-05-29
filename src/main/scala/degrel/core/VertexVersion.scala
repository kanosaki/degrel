package degrel.core

case class VertexVersion(v: Vertex, vb: VertexBody) {
  private[this] val header = v.asInstanceOf[VertexHeader]

  def body: VertexBody = vb

  def ensure(): Boolean = {
    header.body == vb
  }
}
