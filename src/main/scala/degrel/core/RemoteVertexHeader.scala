package degrel.core

class RemoteVertexHeader(_initID: ID) extends VertexHeader(_initID) {
  override def body: VertexBody = ???

  override def write(v: Vertex): Unit = ???

  override def shallowCopy(): Vertex = ???
}
