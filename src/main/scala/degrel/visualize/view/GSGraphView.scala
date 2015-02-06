package degrel.visualize.view

import javafx.embed.swing.SwingNode
import javafx.fxml.FXML

import degrel.core.{Element, Graph, Vertex}
import degrel.ui.ViewBase
import degrel.visualize.utils
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.{Edge, Graph => GSGraph, Node}
import org.graphstream.ui.layout.HierarchicalLayout
import org.graphstream.ui.swingViewer.Viewer

/**
 * Graph viewer using GraphStream.
 */
class GSGraphView extends ViewBase with GraphPresenter {
  protected var graph: Graph = null
  protected var gsgraph: GSGraph = new SingleGraph("FOOBAR")
  protected var viewer: Viewer = null
  //protected var layout: HierarchicalLayout = null
  protected var rootID: String = null

  @FXML
  var drawNode: SwingNode = null

  override def onDocumentLoaded(): Unit = {
    viewer = utils.graphstream.createViewer(gsgraph)
    drawNode.setContent(viewer.getDefaultView)
  }

  def update() = {
    if (graph != null) {
      this.updateModel()
    }
  }

  protected def updateModel() = {
    graph.vertices.foreach(v => {
      val n: Node = gsgraph.addNode(v.id.toString)
      n.setAttribute("ui.label", v.label.expr)
    })
    this.rootID = graph.vertices.head.id.toString
    graph.edges.foreach(e => {
      val from = e.src.id.toString
      val to = e.dst.id.toString
      val gse: Edge = gsgraph.addEdge(s"$from-$to", from, to, true)
      gse.setAttribute("ui.label", e.label.expr)
    })
  }

  override def setData(elem: Element): Unit = {
    gsgraph.clear()
    gsgraph.addAttribute("ui.antialias")
    graph = elem match {
      case v: Vertex => v.toGraph
      case g: Graph => g
    }
    this.update()
    //viewer.disableAutoLayout()
    //this.layout = new HierarchicalLayout()
    //this.layout.setRoots(rootID)
    //viewer.enableAutoLayout(layout)
  }
}
