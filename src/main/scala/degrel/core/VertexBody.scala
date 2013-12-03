package degrel.core


import scalaz._
import Scalaz._

import degrel.engine._
import degrel.utils.IterableExtensions._

case class VertexBody(_label: Label, all_edges: Iterable[Edge]) extends Vertex {
  def label: Label = _label

  private val _edge_cache: Map[Label, Iterable[Edge]] = all_edges.groupBy(e => e.label)
  protected val hasPolyvalentEdge = all_edges.size != _edge_cache.size

  def edges(label: Label): Iterable[Edge] = {
    label match {
      case Label.wildcard => all_edges
      case _ =>
        _edge_cache.get(label) match {
          case None => Seq()
          case Some(a) => a
        }
    }
    if (label == Label.wildcard)
      all_edges
    else
      _edge_cache.get(label) match {
        case None => Seq()
        case Some(a) => a
      }
  }

  def groupedEdges: Iterable[Iterable[Edge]] = {
    _edge_cache.values
  }

  override def toString: String = {
    val edgesExpr = all_edges.map(_.toString).mkString(", ")
    s"${label.expr}($edgesExpr)"
  }

  def matches(pattern: Vertex, context: MatchingContext): MatchedVertex = {
    if (this.label.matches(pattern.label)) {
      MultiplexVertexMatch(this.matchEdges(pattern, context))
    } else {
      NoMatch
    }
  }

  private def matchEdges(pattern: Vertex, context: MatchingContext): Iterable[MatchedVertex] = {
    if (pattern.edges().size > this.edges().size)
      return Seq()
    val edgeGroups = this.groupedEdges.map(this.matchEdgeGroup(_, pattern, context)).toList
    if (edgeGroups.all(_ != Nil)) {
      // sequence([1,2], [3,4], [5,6]]) -> [[1,3,4], [1,3,6], [1,4,5], [1,4,6], ...]
      val edgeMatches = edgeGroups.sequence.toList
      val vertexMatch = VertexBind(this, pattern)
      edgeMatches.map(e => SingleMatchedVertex(vertexMatch, e.flatten))
    } else {
      Seq()
    }
  }

  /**
   *
   * @return List of edge matching
   */
  private def matchEdgeGroup(targetEdgeGroup: Iterable[Edge],
                             pattern: Vertex,
                             context: MatchingContext): List[Seq[MatchedEdge]] = {
    val dataEdges = targetEdgeGroup.toSet
    val targetLabel = targetEdgeGroup.head.label
    val patternEdges = pattern.edges(targetLabel)
    dataEdges.subsets(patternEdges.size).mapFilter(this.matchEdgesSequential(patternEdges, context)).toList
  }

  private def matchEdgesSequential(patternEdges: Iterable[Edge], context: MatchingContext)
                                  (thisEdges: Iterable[Edge]): Option[Seq[MatchedEdge]] = {
    thisEdges.zip(patternEdges).mapAllOrNone {
      case (thisE, patE) => thisE.matches(patE, context) match {
        case NoMatch => None
        case v: MatchedEdge => Some(v)
      }
    }
  }
}
