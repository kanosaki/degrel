package degrel.rewriting

import degrel.core.{Vertex, Edge, Element}

class BuildingContext(val binding: Binding) {
  def matchedVertexExact(patternVertex: Vertex): Vertex = {
    binding.get(patternVertex) match {
      case Some(v) => v match {
        case v: Vertex => v
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching$patternVertex not found in $binding}.")
    }
  }

  def matchedEdgeExact(patternEdge: Edge): Edge = {
    binding.get(patternEdge) match {
      case Some(v) => v match {
        case e: Edge => e
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching$patternEdge not found in $binding}.")
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
}
