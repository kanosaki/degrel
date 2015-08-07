package degrel.engine.rewriting.molding

import degrel.core.{Edge, Label, Vertex, VertexHeader}

trait Molder {
  val header: VertexHeader
  var finishedPhase: MoldPhase = MoldPhase.Initial

  def process(ph: MoldPhase): Unit = {
    if (ph <= finishedPhase) return
    this.onPhase(ph)
    finishedPhase = ph
    this.children.foreach(_.process(ph))
  }

  def children: Iterable[Molder] = {
    mold.edges
      .map(e => context.getMolder(e.dst))
  }

  def onPhase(ph: MoldPhase): Unit = {}

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
