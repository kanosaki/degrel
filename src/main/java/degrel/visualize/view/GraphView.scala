package degrel.visualize.view

import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledThreadPoolExecutor, TimeUnit}
import javafx.beans.value
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{AnchorPane, BorderPane}
import javafx.scene.paint.Color

import degrel.core.{Element, Traverser, Vertex}
import degrel.utils.toRunnable
import degrel.visualize.viewmodel.grapharranger.{DynamicsGraphArranger, GraphArranger}
import degrel.visualize.{GraphicsContextWrapper, UpdateTimer, Vec, ViewBase}

class GraphView extends ViewBase {
  var drawer: GraphArranger = new DynamicsGraphArranger()
  var drawerUpdater: ScheduledExecutorService = null
  val updateTimer = new UpdateTimer(15) {
    override def update(now: Long): Unit = {
      drawGraph()

      if (drawer.isCompleted) {
        stopDrawerUpdates()
        stop()
      }
    }
  }

  // ------------------------------------------
  // FXML Bindings
  // ------------------------------------------
  @FXML
  var drawArea: Canvas = null

  @FXML
  var drawAreaWrap: AnchorPane = null

  @FXML
  var rootPane: BorderPane = null

  @FXML
  def canvasClick(e: MouseEvent) = {

  }

  override def onStopping() = {
    stopDrawerUpdates()
  }

  override def onDocumentLoaded() = {
    drawArea.heightProperty().bind(drawAreaWrap.heightProperty())
    drawArea.widthProperty().bind(drawAreaWrap.widthProperty())
    drawArea.widthProperty().addListener(new value.ChangeListener[Number] {
      override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        drawGraph()
      }
    })

    drawArea.heightProperty().addListener(new value.ChangeListener[Number] {
      override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        drawGraph()
      }
    })
  }

  def drawGraph() = {
    val topLeftPad = Vec(drawArea.getWidth / 10, drawArea.getHeight / 10)
    val g = new GraphicsContextWrapper(drawArea.getGraphicsContext2D)
    val h = drawArea.getHeight
    val w = drawArea.getWidth
    g.clearRect(0, 0, w, h)

    for (e <- drawer.edges) {
      g.setLineWidth(1)
      g.strokeLine(e.from + topLeftPad, e.to + topLeftPad)
      g.setLineWidth(3)
      g.strokeLine(e.to + topLeftPad, (e.to - (e.to - e.from).normalize(40)) + topLeftPad)
    }

    g.setLineWidth(1)

    val vertexW = 60
    val vertexH = 30
    for (v <- drawer.vertices) {
      val loc = v.location + topLeftPad
      g.setFill(Color.color(1, 1, 1))
      g.fillOvalCenter(loc, vertexW, vertexH)
      g.setStroke(Color.color(0, 0, 0))
      g.strokeOvalCenter(loc, vertexW, vertexH)
      g.strokeText(v.origin.label.expr, loc.x - vertexW * 0.3, loc.y + 5)
    }

    if (drawer.isCompleted) {
      g.setFill(Color.color(0.1, 0.1, 0.1))
      g.fillRect(10, 10, 20, 20) // Stop mark..? only for debugging
    }
  }

  // ------------------------------------------
  // Public methods
  // ------------------------------------------
  def setElement(elem: Element) = {
    stopDrawerUpdates()
    drawer.clear()
    elem match {
      case rootV: Vertex => {
        for (v <- Traverser(rootV)) {
          drawer.pushVertex(v)
        }
        drawer.stickVertex(rootV)
      }
    }
    startDrawerUpdates()
  }

  // ------------------------------------------
  // Private methods
  // ------------------------------------------

  private def startDrawerUpdates() = {
    drawerUpdater = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory())
    drawerUpdater.scheduleWithFixedDelay(
    {
      drawer.tick()
    }, 0, 10, TimeUnit.MILLISECONDS)
    updateTimer.start()
  }

  private def stopDrawerUpdates(): Unit = {
    if (drawerUpdater == null) return
    drawerUpdater.shutdown()
    updateTimer.stop()
  }


}
