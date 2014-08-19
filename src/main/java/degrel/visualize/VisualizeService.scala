package degrel.visualize

import degrel.core.Element
import degrel.visualize.view.MultiGraphView

object VisualizeService {
  var multigraphview = new MultiGraphView()

  protected def prepareCurrentPresenter = {
    FXManager.launch()
    val stage = FXUtil.loadStage(new MultiGraphView())
    FXUtil.runLater(
    {
      stage.show()
    })
  }


  def show(elem: Element) = {
    prepareCurrentPresenter
    //prepareCurrentPresenter.pushElement(elem)
  }
}
