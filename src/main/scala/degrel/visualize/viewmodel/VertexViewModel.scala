package degrel.visualize.viewmodel

import degrel.core.Vertex

class VertexViewModel(val target: Vertex) extends ElementViewModel {
  override def toString = target.toString
}
