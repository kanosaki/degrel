package degrel.visualize.viewmodel

import degrel.core.Element
import degrel.visualize.viewmodel.graphdrawer.{DynamicsGraphDrawer, GraphDrawer}

class SimpleGraphPresenterViewModel {
  var graphViewModel = new GraphViewModel()
  var drawer = new DynamicsGraphDrawer()

  def pushElement(elem: Element) = {

  }

  def getGraphViewModel: GraphViewModel = graphViewModel

  def getDrawer: GraphDrawer = drawer
}
