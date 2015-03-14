package degrel.engine.rewriting

import degrel.core.{Edge, Vertex}

import scala.collection.mutable

class BuildingContext(val binding: Binding) {
  val builtVertices = mutable.HashMap[Vertex, Vertex]()

  def registerBuiltVertex(redexV: Vertex, builtV: Vertex) = {
    builtVertices += redexV -> builtV
  }

  def fromBuiltVertex(redexV: Vertex) = {
    builtVertices.get(redexV)
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
