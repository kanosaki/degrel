package degrel.engine.rewriting.molding

import degrel.core.transformer.{CellLimiter, GraphVisitor, TryOwnVisitor}
import degrel.core.{CellBody, Label, Vertex, VertexHeader}
import degrel.engine.rewriting.Binding

class CellMolder(val mold: Vertex, baseContext: MoldingContext) extends Molder {
  val context: MoldingContext = CellMoldingContext(this, baseContext)

  override val header: VertexHeader = context.getHeader(mold)

  override def onPhase(ph: MoldPhase): Unit = ph match {
    case MoldPhase.Mold => {
      this.doMold()
    }
    case MoldPhase.After => {
      // set owner of atoms
      val ownerFixVisitor = GraphVisitor(CellLimiter.default, new TryOwnVisitor(this.header))
      header.edgesWith(Label.E.cellItem).foreach { cellItemEdge =>
        ownerFixVisitor.visit(cellItemEdge.dst)
      }
      // Cell root vertex is owned by parent cell
      header.transferOwner(baseContext.ownerHeader)
    }
    case _ =>
  }

  // Perform as RhsVertex
  /**
    * 自身を書き換え右辺の頂点として，新規に頂点を構成します
    * 自分が左辺でマッチしている頂点，すなわち対応するredex上の頂点を持つ場合は，頂点をマージします
    * また，マッチしていない場合は新規に頂点を構築します
    */
  private def doMold(): Unit = {
    val moldCell = mold.asCell
    val cb = CellBody(this.children.map(_.header),
                      Seq(),
                      Seq(mold.asCell),
                      moldCell.otherEdges,
                      Binding.chain(context.binding, moldCell.binding))
    this.header.write(cb)
  }

  protected def moldItems(items: Iterable[Vertex]): Iterable[Vertex] = {
    items.map { v =>
      context.getMolder(v).header
    }
  }


  // Process only cell items
  override val children: Iterable[Molder] = {
    this.mold.thru(Label.E.cellItem).map(context.getMolder)
  }
}
