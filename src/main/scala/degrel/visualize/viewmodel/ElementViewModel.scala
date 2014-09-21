package degrel.visualize.viewmodel

import degrel.core.{Element, Vertex}

trait ElementViewModel {
  val target: Element

}

object ElementViewModel {
  def create(elem: Element): ElementViewModel = {
    elem match {
      case v: Vertex => new VertexViewModel(v)
    }
  }
}
