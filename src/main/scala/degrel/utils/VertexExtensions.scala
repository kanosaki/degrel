package degrel.utils

import degrel.core

import scala.language.implicitConversions

object VertexExtensions {

  implicit def coreVertexExtension(v: core.Vertex) = new VertexExtensions(v)

  class VertexExtensions(s: core.Vertex) {
    def hasId(id: String): Boolean = {
      s.hasAttr("id", id)
    }
  }
}

