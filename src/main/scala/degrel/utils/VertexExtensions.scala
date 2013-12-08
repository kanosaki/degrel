package degrel.utils

import scala.language.implicitConversions
import degrel.core

object VertexExtensions {
  class VertexExtensions(s: core.Vertex) {
    def hasId(id: String): Boolean = {
      s.hasAttr("id", id)
    }
  }

  implicit def coreVertexExtension(v: core.Vertex) = new VertexExtensions(v)
}

