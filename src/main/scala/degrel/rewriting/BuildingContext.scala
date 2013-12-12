package degrel.rewriting

import degrel.core.Vertex

class BuildingContext(var binding: Binding) {
  def matchOf(patternVertex: Vertex): Vertex = {
    binding.get(patternVertex) match {
      case Some(v) => v match {
        case v: Vertex => v
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException("Invalid matching detected.")
    }
  }

}
