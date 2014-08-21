package degrel.visualize.view

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.fxml.FXML
import javafx.scene.control.{ListCell, ListView}
import javafx.scene.layout.{AnchorPane, BorderPane}
import javafx.util.Callback

import degrel.core.Element
import degrel.visualize.viewmodel.ElementViewModel
import degrel.visualize.{FXUtil, ViewBase}

class MultiGraphView extends ViewBase {
  @FXML
  var graphViewPane: AnchorPane = null
  var graphView = new GraphView()

  @FXML
  var graphsListView: ListView[ElementViewModel] = null
  val rootElements = new scalafx.collections.ObservableBuffer[ElementViewModel]()

  override def onDocumentLoaded() = {
    // グラフ一覧設定
    graphsListView.setCellFactory(new Callback[ListView[ElementViewModel], ListCell[ElementViewModel]] {
      override def call(param: ListView[ElementViewModel]): ListCell[ElementViewModel] = {
        new GraphsListViewCell()
      }
    })
    graphsListView.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[ElementViewModel] {
      override def changed(observable: ObservableValue[_ <: ElementViewModel],
                           oldValue: ElementViewModel,
                           newValue: ElementViewModel): Unit = {
        if (oldValue eq newValue) return
        graphView.setElement(newValue.target)
      }
    })
    graphsListView.setItems(rootElements)

    // メインのグラフ表示部初期化
    val view = FXUtil.loadView[BorderPane](graphView)
    graphViewPane.getChildren.add(view)
  }

  // Event Handlers
  // Public methods
  def pushElement(elem: Element) = {
    val vm = ElementViewModel.create(elem)
    rootElements += vm
    // 表示要素が一つの場合は，自動的に表示を行います
    if (rootElements.size == 1) {
      graphView.setElement(elem)
      graphsListView.getSelectionModel.select(0)
    }
  }

  class GraphsListViewCell extends ListCell[ElementViewModel] {
    override def updateItem(elem: ElementViewModel, empty: Boolean) = {
      super.updateItem(elem, empty)
      empty match {
        case true => {
          setText(null)
          setGraphic(null)
        }
        case false => {
          setText(elem.toString)
          setGraphic(null)
        }
      }
    }
  }

}

