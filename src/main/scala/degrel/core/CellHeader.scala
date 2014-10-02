package degrel.core

class CellHeader(_body: CellBody) extends VertexHeader(_body) with Cell {

  override def toString = {
    this.body.toString
  }
}
