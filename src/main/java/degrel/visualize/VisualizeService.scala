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

  /**
   * {@code elem}を表示します
   * @param elem 表示する要素
   */
  def show(elem: Element): Unit = {
    prepareCurrentPresenter()
    FXUtil.runLater(
    {
      multiGraphView.pushElement(elem)
    })
  }

  def showAndWait(elem: Element): Unit = {
    if (multiGraphView != null) return
    multiGraphView = new MultiGraphView()
    val stage = FXUtil.loadStage(multiGraphView)
    FXUtil.runAndWait(
    {
      multiGraphView.pushElement(elem)
      stage.showAndWait()
    })
  }
}
