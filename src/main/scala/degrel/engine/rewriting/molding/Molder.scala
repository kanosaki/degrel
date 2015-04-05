package degrel.engine.rewriting.molding

import degrel.core.{Edge, Label, Vertex, VertexHeader}

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

  /**
   * Data vertex.
   */
  def othersTarget: Option[Vertex] = {
    this.mold.thru(Label.E.others).toList match {
      case Nil => None
      case v :: Nil => Some(v)
      case _ => throw new RuntimeException("Malformed vertex.")
    }
  }

  /**
   * Data edges.
   */
  def othersEdges: Option[Iterable[Edge]] = {
    this.othersTarget.flatMap { othersV =>
      Some(this.context.unmatchedEdges(othersV))
    }
  }
}

object Molder {
  val phases = Seq(MoldPhase.Scan, MoldPhase.Mold)
}
