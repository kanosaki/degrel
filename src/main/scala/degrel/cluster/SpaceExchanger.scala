package degrel.cluster

import degrel.core._

/** リモートに送信するために，シリアライズ可能な形式へ，ID等を調整しながら変換します
  *
  */
class SpaceExchanger(implicit val node: LocalNode) {
  def packAll(v: Vertex, pushDepth: Int = -1, move: Boolean = false): DGraph = {
    val vertices = pushDepth match {
      case n if n < 0 => Traverser(v)
      case 0 => Seq(v)
      case _ => Traverser(v, pushDepth)
    }
    this.pack(v, vertices, move)
  }

  /**
    * 渡されたVertexのみをpackします
    */
  def pack(root: Vertex, vertices: Iterable[Vertex] = Seq(), move: Boolean = false): DGraph = {
    val packer = new GraphPacker(root, move)
    packer ++= vertices
    packer.pack()
  }

  def packForQuery(root: Vertex, queryOption: QueryOption): DGraph = {
    import QueryOption._
    queryOption match {
      case DepthHint(depth) => this.pack(root, Traverser(root, depth))
      case WholeCell => this.pack(root, Traverser(root))
      case None => this.pack(root)
    }
  }

  def unpack(graph: DGraph, idSpace: DriverIDSpace = IDSpace.global): Vertex = {
    val gr = new GraphUnpacker(graph, node, idSpace)
    gr.root
  }
}
