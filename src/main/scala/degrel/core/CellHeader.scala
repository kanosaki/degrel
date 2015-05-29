package degrel.core

class CellHeader(bdy: CellBody) extends LocalVertexHeader(bdy) with Cell {

  private def bodyAsCell: CellBody = {
    this.body.asInstanceOf[CellBody]
  }

  override def rules: Seq[Rule] = this.bodyAsCell.rules

  override def roots: Seq[Vertex] = this.bodyAsCell.roots

  override def removeRoot(v: Vertex): Unit = this.bodyAsCell.removeRoot(v)

  override def addRoot(v: Vertex): Unit = {
    this.bodyAsCell.addRoot(v)
  }

  override def asCell: Cell = this
}
