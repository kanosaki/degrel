package degrel.engine

import degrel.core

trait MatchBridge[+E <: core.Element] extends Product2[E, E]{

}

case class VertexBridge(patternVertex: core.Vertex, dataVertex: core.Vertex) extends MatchBridge[core.Vertex] {
  def _1: core.Vertex = patternVertex
  def _2: core.Vertex = dataVertex
}

case class EdgeBridge(patternEdge: core.Edge, dataEdge: core.Edge) extends MatchBridge[core.Edge] {
  def _1: core.Edge = patternEdge
  def _2: core.Edge = dataEdge
}

