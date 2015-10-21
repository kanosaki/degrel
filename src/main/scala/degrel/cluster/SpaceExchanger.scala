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
    DGraph(root.id.globalize, vertices.map(mapToDElement).toVector)
  }

  def unpack(graph: DGraph): Vertex = {
    val gr = new GraphRebuilder(graph)
    val ret = gr.get(graph.root).get
    ret
  }

  protected def mapToDElement(v: Vertex): DVertex = {
    def dEdges(): Seq[DEdge] = {
      v.edges.map(mapEdges).toVector
    }
    def dAttrs(): Seq[(String, String)] = {
      v.attributes.map {
        case (k, v) => k.expr -> v
      }.toVector
    }

    v.label match {
      case Label.V.rule => {
        val r = v.asRule
        DRule(v.id.globalize, r.lhs.id.globalize, r.rhs.id.globalize, dAttrs())
      }
      case Label.V.cell => DCell(v.id.globalize, dAttrs(), dEdges(), Seq())
      case _ => DPlainVertex(v.id.globalize, v.label.expr, dAttrs(), dEdges())
    }
  }

  protected def mapEdges(e: Edge): DEdge = {
    DEdge(e.label.expr, e.dst.id.globalize)
  }
}
