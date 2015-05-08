package degrel.engine.rewriting.molding

import degrel.core.{Edge, Label, Vertex, VertexHeader}

trait Molder {
  val header: VertexHeader

  def process(ph: MoldPhase): Unit = {
    mold.edges
      .map(e => context.getMolder(e.dst))
      .foreach(_.process(ph))
    ph match {
      case MoldPhase.After => {
      }
      case _ =>
    }
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
  def othersTargets: Iterable[Vertex] = {
    this.mold.thru(Label.E.others)
  }

  def includeTargets: Iterable[Vertex] = {
    this.mold.thru(Label.E.include)
  }


  /**
   * Data edges.
   */
  def importingEdges: Iterable[Edge] = {
    val othersEdges = this.othersTargets.flatMap { othersV =>
      this.context.unmatchedEdges(othersV)
    }
    val includeVertices = this.includeTargets.map(this.context.matchedVertexExact)
    val includeEdges = includeVertices.flatMap(
      _.edges
    )
    othersEdges ++ includeEdges
  }
}

object Molder {
  val phases = Seq(MoldPhase.Scan, MoldPhase.Mold, MoldPhase.After)
}
