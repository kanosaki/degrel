package degrel.visualize.viewmodel.grapharranger

import degrel.core.Vertex

trait GraphArranger {
  def pushVertex(v: Vertex): Unit

  def stickVertex(v: Vertex): Unit

  def clear(): Unit

  def tick(): Unit

  def vertices: Iterable[VertexViewModel]

  def edges: Iterable[EdgeViewModel]

  def isCompleted: Boolean
}
