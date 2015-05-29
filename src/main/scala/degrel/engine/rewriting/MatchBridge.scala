package degrel.engine.rewriting

import degrel.core

trait MatchBridge[+E <: core.Element] extends Product2[E, E] {

  def confirm(): Boolean
}

case class VertexBridge(patternVertex: core.Vertex,
                        patVersion: core.VertexVersion,
                        dataVertex: core.Vertex,
                        dataVersion: core.VertexVersion,
                        notMatchedEdges: Iterable[core.Edge]) extends MatchBridge[core.Vertex] {
  def _1: core.Vertex = patternVertex

  def _2: core.Vertex = dataVertex

  def confirm() = {
    patVersion.ensure() && dataVersion.ensure()
  }
}

case class EdgeBridge(patternEdge: core.Edge, dataEdge: core.Edge) extends MatchBridge[core.Edge] {
  def _1: core.Edge = patternEdge

  def _2: core.Edge = dataEdge

  def confirm(): Boolean = true
}

