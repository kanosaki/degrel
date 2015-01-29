package degrel.misc.serialize

case class DEdge(label: String, dst: DNode) extends DElement {

  def isRefEdge = dst.isInstanceOf[DRef]
}
