package degrel.misc.serialize

/**
 * `core.Vertex`に対応する`DElement`
 * @param id この頂点の`DDocument`内で一意のID
 * @param label 頂点のラベル
 * @param edges 接続
 * @todo Attributeどうするか
 */
case class DVertex(id: DNodeID, label: String, edges: Seq[DEdge]) extends DNode {

}
