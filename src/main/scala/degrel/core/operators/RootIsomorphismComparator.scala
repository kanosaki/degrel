package degrel.core.operators

import degrel.core._

import scala.collection.mutable
import scalaz._
import Scalaz._

class RootIsomorphismComparator(self: Vertex, another: Vertex) {
  type History = mutable.Map[Vertex, Vertex]

  def eval(): Boolean = {
    val history = new mutable.HashMap[Vertex, Vertex]()
    areIsoVertex(history, self, another)
  }

  private def areIsoVertex(history: History,
                           self: Vertex,
                           that: Vertex): Boolean = {
    history.get(self) match {
      case Some(prev_that) => prev_that == that
      case None => {
        history += self -> that
        self.label == that.label &&
          self.edges.size == that.edges.size &&
          areIsoEdges(history, self, that)
      }
    }
  }

  private def areIsoEdges(history: History,
                          self: Vertex,
                          that: Vertex): Boolean = {
    val self_edges = self.edges.toSeq.sorted
    val that_edges = that.edges.toSeq.sorted
    if (self_edges.size != that_edges.size) return false
    if (self_edges.isEmpty) return true
    val self_grouped_edges = self_edges.groupBy(_.label).values.toSeq
    if (self_grouped_edges.forall(_.size == 1)) {
      // Without polyvalent edges
      areIsoMonovalentEdges(history, self_edges, that_edges)
    } else {
      areIsoPolyvalentEdges(
        history,
        self_grouped_edges,
        that_edges
          .groupBy(_.label)
          .values
          .toSeq)
    }
  }

  /**
   * すべての接続が1価の場合は，ラベルでソートした後にそれぞれの対応する接続について
   * 比較すれば相同である事が確認できます
   */
  private def areIsoMonovalentEdges(history: History,
                                    self_edges: Seq[Edge],
                                    that_edges: Seq[Edge]): Boolean = {
    require(self_edges.size == that_edges.size)
    self_edges
      .zip(that_edges)
      .forall({
      case (e1, e2) => e1.label == e2.label &&
        areIsoVertex(history, e1.dst, e2.dst)
    })
  }

  /**
   * 多価頂点では，どの組み合わせで
   */
  private def areIsoPolyvalentEdges(history: History,
                                    self_grouped_edges: Seq[Seq[Edge]],
                                    that_grouped_edges: Seq[Seq[Edge]]): Boolean = {
    val target_pattern = self_grouped_edges.flatten // 探索する目標となる接続のパターンを決定します
    // 取り得る接続の組み合わせを列挙します
    val candidate_patterns = that_grouped_edges
      .map(_.permutations.toList)
      .toList
      .sequence
      .map(_.flatten)
    // どれか一つでも該当すれば等価である
    candidate_patterns.any(pat => {
      areIsoMonovalentEdges(history.clone(), target_pattern, pat)
    })
  }
}
