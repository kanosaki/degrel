package degrel.visualize

import degrel.core.Element
import degrel.visualize.view.MultiGraphView

object VisualizeService {
  var multiGraphView: MultiGraphView = null

  protected def prepareCurrentPresenter(): Unit = {
    if (multiGraphView != null) return
    multiGraphView = new MultiGraphView()
    val stage = FXUtil.loadStage(multiGraphView)
    FXUtil.runLater(
    {
      stage.show()
    })
  }

  def show(elem: Element) = {
    prepareCurrentPresenter
    FXUtil.runLater(
    {
      multiGraphView.pushElement(elem)
    })
  }
}
