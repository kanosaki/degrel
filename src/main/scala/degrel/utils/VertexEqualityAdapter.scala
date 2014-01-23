package degrel.utils

import degrel.core.Vertex

class VertexEqualityAdapter(target: Vertex) {
  override def equals(other: Any) = other match {
    case v: Vertex => target ==~ v
  }

  override def hashCode() = target.hashCode()
}
