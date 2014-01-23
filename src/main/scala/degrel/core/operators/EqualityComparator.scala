package degrel.core.operators

import degrel.core._
import scala.collection.mutable

// Compare two graph by Structure and ID and Label
class EqualityComparator(self: Vertex, other: Vertex) {
  val selfVertices = new mutable.HashMap[ID, Vertex]()
  val selfEdges = new mutable.HashMap[(ID, ID), Edge]()
  val otherVertices = new mutable.HashMap[ID, Vertex]()
  val otherEdges = new mutable.HashMap[(ID, ID), Edge]()

  def isSame: Boolean = {
    ???
  }

  def aggregateElements(root: Vertex) = {
    for (v <- Traverser(self)) {
      selfVertices += v.id -> v
      for (e <- v.edges()) {
        selfEdges += (e.src.id -> e.dst.id) -> e
      }
    }

    for (v <- Traverser(other)) {
      otherVertices += v.id -> v
      for (e <- v.edges()) {
        otherEdges += (e.src.id -> e.dst.id) -> e
      }
    }
  }

}
