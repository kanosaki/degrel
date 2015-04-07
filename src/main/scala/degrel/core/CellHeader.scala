package degrel.core

class CellHeader(bdy: CellBody) extends VertexHeader with Cell {
  private var _body: CellBody = bdy

  override def write(v: Vertex): Unit = v match {
    case h: VertexHeader => this.write(h.body)
    case cb: CellBody => _body = cb
    case vb: VertexBody => _body = vb.asCellBody
  }

  override def body: VertexBody = _body

  override def shallowCopy(): Vertex = new CellHeader(bdy)

  override def rules: Seq[Rule] = _body.rules

  override def roots: Seq[Vertex] = _body.roots

  override def removeRoot(v: Vertex): Unit = _body.removeRoot(v)

  override def addRoot(v: Vertex): Unit = {
    _body.addRoot(v)
  }
}
