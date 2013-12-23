package degrel.rewriting

import degrel.core.{Vertex, Edge}

class BuildingContext(var binding: Binding) {
  def matchOf(patternVertex: Vertex): Vertex = {
    binding.get(patternVertex) match {
      case Some(v) => v match {
        case v: Vertex => v
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching$patternVertex not found in $binding}.")
    }
  }

  def matchedEdge(patternEdge: Edge): Edge = {
    binding.get(patternEdge) match {
      case Some(v) => v match {
        case e: Edge => e
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching$patternEdge not found in $binding}.")
    }
  }

}
