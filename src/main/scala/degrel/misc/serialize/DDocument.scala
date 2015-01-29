package degrel.misc.serialize

trait DDocument {
  val vertices: Seq[DVertex]

  def lookup(id: DNodeID): Option[DVertex] = idTable.get(id)

  protected lazy val idTable = this.vertices.map(node => node.id -> node).toMap
}
