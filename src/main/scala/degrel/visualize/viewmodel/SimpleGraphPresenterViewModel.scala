package degrel.visualize.viewmodel

import degrel.core.Element

object SimpleGraphPresenterViewModel {
  val DEFAULT_TRAVERSE_DEPTH = 20
}

class SimpleGraphPresenterViewModel() {
  private val graphViewModel: GraphViewModel = new GraphViewModel(SimpleGraphPresenterViewModel.DEFAULT_TRAVERSE_DEPTH)

  def pushElement(elem: Element) = {
    graphViewModel.setElement(elem)
  }

  def getGraphViewModel(): GraphViewModel = {
    graphViewModel
  }

  def dispose(): Unit = {
    graphViewModel.dispose()
  }

}
