package degrel.misc.serialize

import degrel.core.Graph

class FlatDocument(src: Graph) extends DDocument {

  override val vertices: Seq[DVertex] = {
    implicit val self = this
    val idTable = src.vertices.zipWithIndex.toMap

    src.vertices.zipWithIndex.map {
      case (v, index) => {
        val edges = v.
          edges().
          map(e => DEdge(e.label.expr, DRef(idTable(e.dst)))).
          toSeq
        DVertex(index, v.label.expr, edges)
      }
    }
  }
}
