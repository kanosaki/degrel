package degrel.core

import degrel.engine.rewriting.{MonoVertexMatching, PolyVertexMatching, VertexBridge, _}
import degrel.utils.IterableExtensions._

import scalaz.Scalaz._

case class Matcher(self: Vertex) {
  // Perform as LhsVertex
  def matches(pattern: Vertex, context: MatchingContext): VertexMatching = {
    val patVersion = pattern.version
    val selfVersion = self.version
    if (!self.label.matches(pattern.label))
      return NoMatching
    if (pattern.edges.isEmpty) {
      return MonoVertexMatching(VertexBridge(pattern, patVersion, self, selfVersion, self.edges), Stream())
    }
    val matchCombinations = this.matchEdges(pattern, context, patVersion, selfVersion)
    if (matchCombinations.isEmpty) {
      NoMatching
    } else if (matchCombinations.size == 1) {
      matchCombinations.head
    } else {
      PolyVertexMatching(matchCombinations)
    }
  }

  private def matchEdges(pattern: Vertex, context: MatchingContext, patV: VertexVersion, selfV: VertexVersion): Seq[VertexMatching] = {
    if (pattern.edges.size > self.edges.size)
      return Stream()
    val edgeGroups = pattern.groupedEdges.map(this.matchEdgeGroup(_, context)).toList
    if (edgeGroups.forall(!_.isEmpty)) {
      // sequence([1,2], [3,4], [5,6]]) -> [[1,3,4], [1,3,6], [1,4,5], [1,4,6], ...]
      val edgeMatches = edgeGroups.sequence.toStream
      edgeMatches.map(e => {
        val matchedEdgeMatchings = e.flatten[EdgeMatching]
        val matchedDataEdges = matchedEdgeMatchings.map(_.bridge.dataEdge)
        val unmetchedEdges = self.edges.toSet &~ matchedDataEdges.toSet
        val vertexMatch = VertexBridge(pattern, patV, self, selfV, unmetchedEdges)
        MonoVertexMatching(vertexMatch, matchedEdgeMatchings.toStream)
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
    val dataEdges = self.edgesWith(targetLabel).toSet
    if (dataEdges.isEmpty || dataEdges.size < patternEdges.size)
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
