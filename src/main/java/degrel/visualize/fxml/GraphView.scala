package degrel.visualize.fxml

import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{AnchorPane, BorderPane}

import degrel.visualize.{GCWrapper, UpdateTimer, ViewBase}

class GraphView extends ViewBase {
  val updateTimer = new UpdateTimer(10) {
    override def update(now: Long): Unit = {
      val g = new GCWrapper(drawArea.getGraphicsContext2D)
      val h = drawArea.getHeight
      val w = drawArea.getWidth
      g.clearRect(0, 0, w, h)
    }
  }
  updateTimer.start()

  @FXML
  var drawArea: Canvas = null

  @FXML
  var drawAreaWrap: AnchorPane = null

  @FXML
  var rootPane: BorderPane = null

  @FXML
  def canvasClick(e: MouseEvent) = {

  }

  override def onDocumentLoaded() = {
    drawArea.heightProperty().bind(drawAreaWrap.heightProperty())
    drawArea.widthProperty().bind(drawAreaWrap.widthProperty())
  }
}
