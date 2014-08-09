package degrel.core

case class VertexVersion(v: Vertex, vb: VertexBody) {
  private val header = v.asInstanceOf[VertexHeader]

  def body: VertexBody = vb

  def ensure(): Boolean = {
    header.body.id == vb.id
  }
}