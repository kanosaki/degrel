package degrel.visualize

import degrel.core.Element

object VisualizeService {
  private var _currentPresenter: SimpleGraphPresenter = null

  protected def prepareCurrentPresenter = {
    if (_currentPresenter == null) {
      _currentPresenter = new SimpleGraphPresenter()
    }
    _currentPresenter.pack()
    _currentPresenter.setVisible(true)
    _currentPresenter
  }


  def show(elem: Element) = {
    prepareCurrentPresenter.pushElement(elem)
  }

}
