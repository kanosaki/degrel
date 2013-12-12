package degrel.core

import scalaz._
import Scalaz._
import degrel.utils.IterableExtensions._
import degrel.rewriting._
import degrel.rewriting.PolyVertexMatching
import degrel.rewriting.VertexBridge
import degrel.rewriting.MonoVertexMatching

case class Matcher(self: Vertex) {
  // Perform as LhsVertex
  def matches(pattern: Vertex, context: MatchingContext): VertexMatching = {
    if (!self.label.matches(pattern.label))
      return NoMatching
    if (pattern.edges().size == 0) {
      return MonoVertexMatching(VertexBridge(pattern, self), Stream())
    }
    val matchCombinations = this.matchEdges(pattern, context)
    if (matchCombinations.isEmpty) {
      NoMatching
    } else if(matchCombinations.size == 1) {
      matchCombinations.head
    } else {
      PolyVertexMatching(matchCombinations)
    }
  }

  private def matchEdges(pattern: Vertex, context: MatchingContext): Seq[VertexMatching] = {
    if (pattern.edges().size > self.edges().size)
      return Stream()
    val edgeGroups = pattern.groupedEdges.map(this.matchEdgeGroup(_, context)).toList
    if (edgeGroups.forall(!_.isEmpty)) {
      // sequence([1,2], [3,4], [5,6]]) -> [[1,3,4], [1,3,6], [1,4,5], [1,4,6], ...]
      val edgeMatches = edgeGroups.sequence.toStream
      val vertexMatch = VertexBridge(self, pattern)
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
    val dataEdges = self.edges(targetLabel).toSet
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
