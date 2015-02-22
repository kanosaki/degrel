package degrel.core

import degrel.engine.rewriting.BuildingContext

class CellHeader(bdy: CellBody) extends VertexHeader with Cell {
  private var _body: CellBody = bdy

  override def write(v: Vertex): Unit = v match {
    case cb: CellBody => _body = cb
  }

  override def body: VertexBody = _body

  override def shallowCopy(): Vertex = new CellHeader(bdy)

  override def rules: Seq[Rule] = _body.rules

  override def roots: Seq[Vertex] =_body.roots
}
