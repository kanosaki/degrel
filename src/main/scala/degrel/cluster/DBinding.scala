package degrel.cluster

import degrel.core.ID
import degrel.engine.rewriting.Binding

case class DBinding(items: Seq[DBindBridge]) {
  def unpack(): Binding = {
    new Binding(Seq())
  }
}

object DBinding {
  def pack(binding: Binding) = new DBinding(Seq())
}

trait DBindBridge

case class VertexBridge(patVertex: ID, redexVertex: ID) extends DBindBridge

case class EdgeBridge(patEdge: (ID, ID), redexEdge: (ID, ID)) extends DBindBridge
