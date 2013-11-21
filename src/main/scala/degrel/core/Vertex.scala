package degrel.core

trait Vertex {

}

object Vertex {
  def apply(label: String, edges: Iterable[(String, Vertex)]): Vertex = {
    val coreEdges = edges.
      map {
      case (s, v) => Edge(Label(s), v.asInstanceOf[VertexHeader])
    }.toSeq
    val body = VertexBody(Label(label), coreEdges)
    new VertexHeader(body)
  }
}

