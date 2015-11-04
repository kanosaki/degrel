package degrel.core

import degrel.engine.rewriting.molding.MoldingContextBase

class ReferenceVertexBody(label: Label, attrs: Map[Label, String], all_edges: Iterable[Edge])
  extends LocalVertexBody(
    label,
    attrs,
    all_edges) {

  def referenceTarget: Vertex = {
    val refEdges = this.edgesWith(SpecialLabels.E_REFERENCE_TARGET)
    refEdges.head.dst
  }
}

