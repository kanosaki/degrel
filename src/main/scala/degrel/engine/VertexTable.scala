package degrel.engine

import degrel.core
import degrel.utils.collection.mutable.WeakMultiMap

class VertexTable {
  private[this] val map = new WeakMultiMap[core.Label, core.Vertex]()

  def get(label: core.Label): Option[Set[core.Vertex]] = {
    map.get(label) match {
      case Some(vs) => Some(vs.toSet)
      case _ => None
    }
  }

  def put(label: core.Label, vertex: core.Vertex): this.type = {
    map.addBinding(label, vertex)
    this
  }
}
