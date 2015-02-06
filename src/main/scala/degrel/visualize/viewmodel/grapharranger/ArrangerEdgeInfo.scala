package degrel.visualize.viewmodel.grapharranger

import degrel.core.Edge
import degrel.ui.Vec

trait ArrangerEdgeInfo {
  def origin: Edge

  def from: Vec

  def to: Vec
}
