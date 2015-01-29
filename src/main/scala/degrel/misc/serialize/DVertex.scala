package degrel.misc.serialize

case class DVertex(id: DNodeID, label: String, edges: Seq[DEdge]) extends DNode {

}
