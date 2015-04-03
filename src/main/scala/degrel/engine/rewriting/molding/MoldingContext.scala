package degrel.engine.rewriting.molding

import degrel.core.{Edge, Vertex}
import degrel.engine.rewriting.Binding

import scala.collection.mutable

class MoldingContext(val binding: Binding, val factory: MolderFactory) {
  private[this] val molderMapping = mutable.HashMap[Vertex, Molder]()

  def addMolder(mold: Vertex, molder: Molder) = {
    molderMapping += mold -> molder
  }

  def getMolder(mold: Vertex): Molder = {
    molderMapping.getOrElseUpdate(mold, factory.get(mold, this))
  }

  def matchedVertexExact(patternVertex: Vertex): Vertex = {
    binding.get(patternVertex) match {
      case Some(v) => v match {
        case v: Vertex => v
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching: $patternVertex not found in $binding}.")
    }
  }

  def matchedEdgeExact(patternEdge: Edge): Edge = {
    binding.get(patternEdge) match {
      case Some(v) => v match {
        case e: Edge => e
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching: $patternEdge not found in $binding}.")
    }
  }

  def matchedVertex(patternVertex: Vertex): Option[Vertex] = {
    binding.get(patternVertex) match {
      case Some(v) => Some(v.asInstanceOf[Vertex])
      case None => None
    }
  }

  def matchedEdge(patternEdge: Edge): Option[Edge] = {
    binding.get(patternEdge) match {
      case Some(e) => Some(e.asInstanceOf[Edge])
      case None => None
    }
  }

  def unmatchedEdges(patternVertex: Vertex): Iterable[Edge] = {
    binding.unmatchedEdges(matchedVertexExact(patternVertex))
  }
}
