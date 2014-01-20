package degrel.core.operators

import degrel.core.{Trajectory, Vertex, Edge}
import scala.collection.mutable

// Compare two graph by Structure and ID and Label
class EqualityComparator(self: Vertex, another: Vertex) {
  val history = new Trajectory()
  val vQueue = new mutable.Queue[Vertex]()

  vQueue += self

  def compare(): Boolean = {
    this.compareGraph(self)
  }

  def compareGraph(next: Vertex): Boolean = {
    if(history.isStamped(next)) return true
    ???

  }

  def isSameVertex(v1: Vertex, v2: Vertex): Boolean = {
    (v1 eq v2) || (this.compareVertex(v1, v2) && this.compareEdges(v1, v2))
  }

  def compareVertex(v1: Vertex, v2: Vertex): Boolean = {
    v1.label == v2.label
  }

  def compareEdges(v1: Vertex, v2: Vertex): Boolean = {
    val v1edges = v1.edges().map(new EdgeCompareHelper(_)).toSet
    val v2edges = v2.edges().map(new EdgeCompareHelper(_)).toSet
    if (v1edges.size != v2edges.size) return false
    v1edges == v2edges
  }

  class EdgeCompareHelper(selfE: Edge) {
    override def equals(other: Any) = other match {
      case e: Edge => e.label == selfE.label && compareVertex(selfE.dst, e.dst)
      case _ => false
    }

    override def hashCode = {
      val prime = 149
      var result = 1
      result = prime * result + selfE.label.hashCode()
      result
    }
  }

}

object EqualityComparator {
  def apply(a: Vertex, b: Vertex): Boolean = {
    new EqualityComparator(a, b).compare()
  }
}
