package degrel.core

class LocalVertexHeader(private var _body: VertexBody) extends VertexHeader {

  override def body: VertexBody = _body

  override def write(v: Vertex): Unit = {
    v match {
      case vh: VertexHeader => _body = vh.body
      case vb: VertexBody => _body = vb
    }
    _body.header = this
  }

  override def shallowCopy(): Vertex = ???
}
