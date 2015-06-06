package degrel

import scala.language.implicitConversions

object Query {
  implicit def vertexExtension(v: core.Vertex) = new degrel.utils.tonberry.VertexExtension(v)
}
