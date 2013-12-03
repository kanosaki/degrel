package degrel.engine

import degrel.core

trait MatchBind[+E <: core.Element] extends Product2[E, E]{

}

case class VertexBind(patternVertex: core.Vertex, dataVertex: core.Vertex) extends MatchBind[core.Vertex] {
  def _1: core.Vertex = patternVertex
  def _2: core.Vertex = dataVertex
}

case class EdgeBind(patternEdge: core.Edge, dataEdge: core.Edge) extends MatchBind[core.Edge] {
  def _1: core.Edge = patternEdge
  def _2: core.Edge = dataEdge
}

