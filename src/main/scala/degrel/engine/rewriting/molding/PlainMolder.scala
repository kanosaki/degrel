package degrel.engine.rewriting.molding

import degrel.core._

/**
 * 通常の頂点が`Mold`の場合に使用します
 * @param mold
 * @param context
 */
class PlainMolder(val mold: Vertex, val context: MoldingContext) extends Molder {
  override val header: VertexHeader = context.getHeader(mold)

  override def process(ph: MoldPhase): Unit = {
    ph match {
      case MoldPhase.Mold => {
        this.doMold()
      }
      case _ =>
    }
    super.process(ph)
  }

  // Perform as RhsVertex
  /**
   * 自身を書き換え右辺の頂点として，新規に頂点を構成します
   * 自分が左辺でマッチしている頂点，すなわち対応するredex上の頂点を持つ場合は，頂点をマージします
   * また，マッチしていない場合は新規に頂点を構築します
   */
  private def doMold(): Unit = {
    if (mold.hasEdge(Label.E.others)) {
      val plainEdges = mold.edges.filter(!_.isOthers)
      val builtEdges = othersEdges.getOrElse(Seq()) ++ moldEdges(plainEdges)
      val vb = VertexBody(mold.label, mold.attributes, builtEdges.toSeq, ID.autoAssign)
      this.header.write(vb)
    } else {
      val builtEdges = moldEdges(mold.edges)
      val vb = VertexBody(mold.label, mold.attributes, builtEdges.toSeq, ID.autoAssign)
      this.header.write(vb)
    }
  }
}
