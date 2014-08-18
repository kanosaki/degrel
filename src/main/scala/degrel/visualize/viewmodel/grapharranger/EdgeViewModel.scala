package degrel.visualize.viewmodel.grapharranger

import degrel.core.Edge
import degrel.visualize.Vec

trait EdgeViewModel {
  def origin: Edge

  def from: Vec

  def to: Vec
}
