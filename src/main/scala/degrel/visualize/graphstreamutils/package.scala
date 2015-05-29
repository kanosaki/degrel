package degrel.visualize

import org.graphstream.graph.Graph
import org.graphstream.ui.layout.{Layout, Layouts}
import org.graphstream.ui.swingViewer.Viewer.ThreadingModel
import org.graphstream.ui.swingViewer.{GraphRenderer, Viewer}

package object utils {
  object graphstream {
    def createViewer(graph: Graph,
                     viewID: String = Viewer.DEFAULT_VIEW_ID,
                     layout: Option[Layout] = Some(Layouts.newLayoutAlgorithm()),
                     openFrame: Boolean = false,
                     threading: ThreadingModel = Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD) = {
      val viewer: Viewer = new Viewer(graph, threading)
      val renderer: GraphRenderer = Viewer.newGraphRenderer
      viewer.addView(viewID, renderer, openFrame)

      layout match {
        case Some(l) => viewer.enableAutoLayout(l)
        case None =>
      }
      viewer
    }
  }
}
