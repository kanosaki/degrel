package degrel.engine.rewriting.molding

import degrel.core.{Edge, Vertex, VertexHeader}

trait Molder {
  val header: VertexHeader

  def process(ph: MoldPhase): Unit = {
    mold.edges
      .map(e => context.getMolder(e.dst))
      .foreach(_.process(ph))
  }

  def mold: Vertex

  def context: MoldingContext

  protected def moldEdges(edges: Iterable[Edge]): Iterable[Edge] = {
    edges.map { e =>
      val nextMolder = context.getMolder(e.dst)
      Edge(e.src, e.label, nextMolder.header)
    }
  }
}

object Molder {
  val phases = Seq(MoldPhase.Scan, MoldPhase.Mold)
}
