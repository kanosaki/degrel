package degrel.visualize.view

import javafx.embed.swing.SwingNode
import javafx.fxml.FXML
import javax.swing.{ScrollPaneConstants, JScrollPane}

import com.mxgraph.layout._
import com.mxgraph.model.mxGraphModel
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.swing.util.mxMorphing
import com.mxgraph.util.mxEventSource.mxIEventListener
import com.mxgraph.util.{mxEvent, mxEventObject}
import com.mxgraph.view.mxGraph
import degrel.core.{Element, Graph, ID, Vertex}
import degrel.visualize.ViewBase

import scala.collection.mutable

/**
 * Graph viewer using JGraphX
 */
class JGraphView extends ViewBase with GraphPresenter {
  protected var graph: Graph = null
  protected var view: mxGraph = new mxGraph()
  protected var viewComponent: mxGraphComponent = new mxGraphComponent(view)
  viewComponent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
  viewComponent.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
  viewComponent.setGridVisible(true)
  viewComponent.setGridStyle(mxGraphComponent.GRID_STYLE_LINE)
  viewComponent.setDragEnabled(false)

  protected var idMapping: mutable.HashMap[ID, AnyRef] = null

  @FXML
  var drawNode: SwingNode = null

  override def onDocumentLoaded(): Unit = {
    drawNode.setContent(viewComponent)
  }

  // http://forum.jgraph.com/questions/4810/how-to-layout-nodes-automatically-using-fast-organic-layout
  def update() = {
    if (graph != null) {
      this.updateModel()
      this.updateLayout()
    }
  }

  protected def updateLayout() = {
    //val layout = new mxHierarchicalLayout(view, SwingConstants.NORTH)
    //val layout = new mxOrthogonalLayout(view)
    val layout = new mxFastOrganicLayout(view)
    //val layout = new mxCompactTreeLayout(view)
    //val layout = new mxOrganicLayout(view)
    //val layout = new mxPartitionLayout(view)
    //val layout = new mxParallelEdgeLayout(view)
    view.getModel.beginUpdate()
    try {
      layout.execute(view.getDefaultParent)
    } catch {
      case e: Throwable => e.printStackTrace()
    } finally {
      val morph = new mxMorphing(viewComponent)
      morph.addListener(mxEvent.DONE, new mxIEventListener() {
        override def invoke(sender: scala.Any, evt: mxEventObject): Unit = {
          view.getModel.endUpdate()
        }
      })
      morph.startAnimation()
    }
  }

  protected def updateModel() = {
    view.getModel.beginUpdate()
    val parent = view.getDefaultParent
    try {
      idMapping = new mutable.HashMap()
      graph.vertices.foreach(v => {
        val obj = view.insertVertex(parent, null, v.label.expr, 0, 0, 50, 30)
        idMapping += v.id -> obj
      })
      graph.edges.foreach(e => {
        val from = idMapping(e.src.id)
        val to = idMapping(e.dst.id)
        view.insertEdge(parent, null, e.label.expr, from, to)
      })
    } catch {
      case e: Throwable => e.printStackTrace()
    } finally {
      view.getModel.endUpdate()
    }
  }

  override def setData(elem: Element): Unit = {
    view.setModel(new mxGraphModel())
    graph = elem match {
      case v: Vertex => v.toGraph
      case g: Graph => g
    }
    this.update()
  }
}

