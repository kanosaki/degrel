package degrel.core.validate

import degrel.core.{VertexHeader, Vertex}

import scalaz.Validation

/**
 *
 */
class VertexValidator(val root: Vertex) {


  class HeaderRule {
    def apply(vh: VertexHeader) = {
      assert(vh.body != null)
    }
  }

}
