package degrel.core

class RemoteVertexHeader(override val id: ID) extends VertexHeader {
  override def body: VertexBody = ???

  override def write(v: Vertex): Unit = ???

  override def shallowCopy(): Vertex = ???
}
