package degrel.visualize.view

import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledThreadPoolExecutor, TimeUnit}
import javafx.beans.value
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{AnchorPane, BorderPane}
import javafx.scene.paint.Color

import degrel.core.{Graph, Element, Traverser, Vertex}
import degrel.ui.{ViewBase, UpdateTimer, Vec, GraphicsContextWrapper}
import degrel.utils.toRunnable
import degrel.visualize.viewmodel.grapharranger.{DynamicsGraphArranger, GraphArranger}

class GraphView extends ViewBase with GraphPresenter {
  val updateTimer = new UpdateTimer(15) {
    override def update(now: Long): Unit = {
      drawGraph()

      if (drawer.isCompleted) {
        stopDrawerUpdates()
        stop()
      }
    }
  }
  var drawer: GraphArranger = new DynamicsGraphArranger()
  var drawerUpdater: ScheduledExecutorService = null
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

  private def stopDrawerUpdates(): Unit = {
    if (drawerUpdater == null) return
    drawerUpdater.shutdown()
    updateTimer.stop()
  }

  override def onDocumentLoaded() = {
    // 描画領域のサイズが外側に応じて変化するようにbind
    drawArea.heightProperty().bind(drawAreaWrap.heightProperty())
    drawArea.widthProperty().bind(drawAreaWrap.widthProperty())
    // 描画領域のサイズが変更されたら再描画
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
    val g = new GraphicsContextWrapper(drawArea.getGraphicsContext2D)
    val h = drawArea.getHeight
    val w = drawArea.getWidth
    val vertexW: Double = 60
    val vertexH: Double = 30

    g.setTransform(1, 0, 0, 1, 0, 0)
    g.clearRect(0, 0, w, h)
    // translateは蓄積してしまうので，setTransformを使ってTransformのリセットも同時に行う
    g.setTransform(1, 0, 0, 1, drawArea.getWidth / 10, drawArea.getHeight / 10)
    g.clearRect(0, 0, w, h)


    // 接続を描画
    for (e <- drawer.edges) {
      // 頂点は現状楕円で描画されいるので，楕円と接続を表すベクトルとの交点を求めて，
      // 接続の矢印を描画します
      val rev = e.from - e.to
      val p = rev.x
      val q = rev.y
      val a = vertexW / 2
      val b = vertexH / 2
      val crossX = (a * b * p) / math.sqrt(math.pow(a * q, 2) + math.pow(b * p, 2)) + e.to.x
      val crossY = (a * b * q) / math.sqrt(math.pow(a * q, 2) + math.pow(b * p, 2)) + e.to.y
      g.setFill(Color.BLACK)
      g.fillArrow(e.from, Vec(crossX, crossY))
    }

    g.setLineWidth(1)

    // 頂点を描画
    for (v <- drawer.vertices) {
      val loc = v.location
      g.setFill(Color.WHITE)
      g.fillOvalCenter(loc, vertexW, vertexH)
      g.setStroke(Color.BLACK)
      g.strokeOvalCenter(loc, vertexW, vertexH)
      g.strokeText(v.origin.label.expr, loc.x - vertexW * 0.3, loc.y + 5)
    }


    // デバッグ用に停止マークを描画
    if (drawer.isCompleted) {
      g.setFill(Color.color(0.1, 0.1, 0.1))
      g.fillRect(10, 10, 20, 20) // Stop sign..? only for debugging
    }
  }

  // ------------------------------------------
  // Private methods
  // ------------------------------------------

  // ------------------------------------------
  // Public methods
  // ------------------------------------------
  override def setData(elem: Element) = {
    elem match {
      case rootV: Vertex => {
        this.setGraph(rootV.toGraph)
      }
      case g: Graph => {
        this.setGraph(g)
      }
    }
  }

  private def startDrawerUpdates() = {
    drawerUpdater = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory())
    drawerUpdater.scheduleWithFixedDelay(
    {
      drawer.tick()
    }, 0, 10, TimeUnit.MILLISECONDS)
    updateTimer.start()
  }

  def setGraph(g: Graph): Unit = {
    stopDrawerUpdates()
    drawer.clear()
    g.vertices.foreach(drawer.pushVertex)
    drawer.stickVertex(g.vertices.head)
    startDrawerUpdates()
  }
}
