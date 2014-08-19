package degrel.visualize.view

import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.scene.layout.{BorderPane, AnchorPane}

import degrel.visualize.{FXUtil, ViewBase}
import degrel.visualize.viewmodel.grapharranger.VertexViewModel

class MultiGraphView extends ViewBase {
  @FXML
  var graphViewPane: AnchorPane = null

  @FXML
  var graphsListView: ListView[VertexViewModel] = null

  override def onSceneSet(s: Scene) = {
    val view = FXUtil.loadView[BorderPane](new GraphView())
    graphViewPane.getChildren.add(view)
  }
}

