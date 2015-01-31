package degrel.misc.serialize

import degrel.core.Graph

/**
 * すべての接続を`DRef`として表現する`DDocument`
 * すべての接続を`DRef`で表現するので，結果的にすべての`DVertex`は
 * 兄弟ノードとなり木構造となりません
 * @param src
 */
class FlatDocument(src: Graph) extends DDocument {

  override val vertices: Seq[DVertex] = {
    implicit val self = this
    val idTable = src.vertices.zipWithIndex.toMap

    src.vertices.zipWithIndex.map {
      case (v, index) => {
        val edges = v.
          edges().
          map(e => DEdge(idTable(v), e.label.expr, DRef(idTable(e.dst)))).
          toSeq
        DVertex(index, v.label.expr, edges)
      }
    }
  }
}
