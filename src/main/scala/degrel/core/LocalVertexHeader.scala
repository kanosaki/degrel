package degrel.core

class LocalVertexHeader(initBody: VertexBody) extends VertexHeader {
  private var _body: VertexBody = initBody

  override def body: VertexBody = _body

  override def write(v: Vertex): Unit = v match {
    case vh: VertexHeader => _body = vh.body
    case vb: VertexBody => _body = vb
  }

  override def shallowCopy(): Vertex = ???
}
