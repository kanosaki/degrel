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

  override def toString = this.repr


  def repr: String = {
    s"${label.expr}"
  }

  def reprRecursive: String = {
    val edgesExpr = all_edges.map(_.toString).mkString(", ")
    s"${this.repr}($edgesExpr)"
  }

  def matches(pattern: Vertex, context: MatchingContext): VertexMatching = {
    if (!this.label.matches(pattern.label))
      return NoMatching
    if (pattern.edges().size == 0) {
      return MonoVertexMatching(VertexBridge(pattern, this), Stream())
    }
    val matchCombinations = this.matchEdges(pattern, context)
    if (matchCombinations.isEmpty) {
      NoMatching
    } else {
      PolyVertexMatching(matchCombinations)
    }
  }

  private def matchEdges(pattern: Vertex, context: MatchingContext): Iterable[VertexMatching] = {
    if (pattern.edges().size > this.edges().size)
      return Stream()
    val edgeGroups = pattern.groupedEdges.map(this.matchEdgeGroup(_, context)).toList
    if (edgeGroups.forall(!_.isEmpty)) {
      // sequence([1,2], [3,4], [5,6]]) -> [[1,3,4], [1,3,6], [1,4,5], [1,4,6], ...]
      val edgeMatches = edgeGroups.sequence.toStream
      val vertexMatch = VertexBridge(this, pattern)
      edgeMatches.map(e => {
        val matchSeq = e.flatten
        MonoVertexMatching(vertexMatch, matchSeq)
      })
    } else {
      Stream()
    }
  }

  /**
   *
   * @return List of edge matching
   */
  private def matchEdgeGroup(patternEdgesIt: Iterable[Edge],
                             context: MatchingContext): List[Seq[EdgeMatching]] = {
    val patternEdges = patternEdgesIt.toSeq
    val targetLabel = patternEdges.head.label
    val dataEdges = this.edges(targetLabel).toSet
    if (dataEdges.size == 0 || dataEdges.size < patternEdges.size)
      return Nil
    val combination = dataEdges.subsets(patternEdges.size).toStream.flatMap(_.toSeq.permutations)
    val filtered = combination.mapFilter(this.matchEdgesSequential(patternEdges, context)).toList
    filtered
  }

  private def matchEdgesSequential(patternEdges: Iterable[Edge], context: MatchingContext)
                                  (thisEdges: Iterable[Edge]): Option[Seq[EdgeMatching]] = {
    thisEdges.zip(patternEdges).mapAllOrNone {
      case (thisE, patE) => thisE.matches(patE, context) match {
        case NoMatching => None
        case v: EdgeMatching => Some(v)
      }
    }
  }
}
