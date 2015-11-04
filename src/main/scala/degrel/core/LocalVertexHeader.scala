package degrel.core

class LocalVertexHeader(protected var _body: VertexBody, _initID: ID = ID.nextLocalVertexID()) extends VertexHeader(_initID) {
  override def body: VertexBody = _body

  override def write(v: Vertex): Unit = {
    v match {
      case vh: VertexHeader => _body = vh.body
      case vb: VertexBody => _body = vb
    }
    _body.header = this
  }

  def clearReverseFingerprint(depth: Int): Unit = {

  }

  override def shallowCopy(): Vertex = ???
}
