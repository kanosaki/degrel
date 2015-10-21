package degrel.core

import degrel.engine.rewriting.Binding

class CellHeader(bdy: CellBody, override val id: ID = ID.autoAssign) extends LocalVertexHeader(bdy) with Cell {

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

  /**
   * このCellを直接内包するCell
   */
  override def parent: Cell = ???

  /**
   * この`Cell`の元になるCell．
   * 規則を継承します
   */
  override def bases: Seq[Cell] = ???

  override def otherEdges: Seq[Edge] = this.bodyAsCell.otherEdges

  override def binding: Binding = this.bodyAsCell.binding
}
