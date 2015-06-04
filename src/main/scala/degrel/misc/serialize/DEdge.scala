package degrel.misc.serialize

/**
 * core.Edgeに対応する`DElement`
 * @param label 接続のラベル
 * @param dst 接続先の`DNode`
 */
case class DEdge(srcID: DNodeID, label: String, dst: DNode) extends DElement {

  def isRefEdge = dst.isInstanceOf[DRef]

  def src(implicit dDocument: DDocument) = dDocument.lookup(this.srcID).get
}
