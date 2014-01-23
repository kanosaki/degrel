package degrel.core


object Graph {
  def getVertexBody(v: Vertex): VertexBody = v match {
    case vh: VertexHeader => vh.body
    case vb: VertexBody => vb
  }
}

/**
 * グラフのrootとして振る舞える頂点．パターンマッチ用のラベルテーブル等を持つ
 * @param root
 */
class Graph(val root: Vertex) extends VertexHeader(Graph.getVertexBody(root)) {
}
