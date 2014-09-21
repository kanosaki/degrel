package degrel

import degrel.core.Element

package object visualize {
  def show(elem: Element) = VisualizeService.show(elem)
}
