package degrel.visualize.viewmodel.grapharranger

import degrel.core.Vertex
import degrel.ui.Vec

trait ArrangerVertexInfo {
  def location: Vec

  def origin: Vertex
}
