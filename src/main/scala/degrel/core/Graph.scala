package degrel.core

import degrel.core.utils.PrettyPrintOptions


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
trait Graph extends Element {
  def vertices: Seq[Vertex]

  def edges: Seq[Edge]

  def findEdges(src: Vertex = null, dst: Vertex = null): Iterable[Edge] = {
    val src_filtered = if (src != null) {
      this.edges.filter(_.src == src)
    } else {
      this.edges
    }

    val dst_filtered = if (dst != null) {
      src_filtered.filter(_.dst == dst)
    } else {
      src_filtered
    }
    dst_filtered
  }
}

trait RootedGraph extends Graph {
  def root: Vertex
}

class RawRootedGraph(_root: Vertex) extends RootedGraph {

  def root: Vertex = _root

  def vertices: Seq[Vertex] = {
    Traverser(root).toSeq
  }

  def edges: Seq[Edge] = {
    Traverser(root).flatMap(_.edges).toSeq
  }

  override def pp(implicit opt: PrettyPrintOptions = PrettyPrintOptions.default): String = {
    s"Graph(root=${root.pp()})"
  }
}

class FrozenRootedGraph(val root: Vertex, val vertices: Seq[Vertex]) {
  assert(vertices.contains(root))

  val edges = vertices.flatMap(_.edges)
}

