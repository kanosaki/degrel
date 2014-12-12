package degrel

import degrel.core.Element

package object visualize {
  def show(elem: Element) = VisualizeService.show(elem)

  def showAndWait(elem: Element) = VisualizeService.showAndWait(elem)
}
