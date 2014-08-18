package degrel.visualize.viewmodel.grapharranger

import degrel.core.Vertex
import degrel.visualize.Vec

trait VertexViewModel {
  def location: Vec

  def origin: Vertex
}
