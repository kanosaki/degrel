package degrel.core

import degrel.cluster.LocalNode

class RemoteVertexHeader(_initID: ID, node: LocalNode) extends VertexHeader(_initID) {
  override def body: VertexBody = ???

  override def write(v: Vertex): Unit = ???

  override def shallowCopy(): Vertex = ???
}
