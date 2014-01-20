package degrel.core.operators

import degrel.core.Vertex

class Duplicator(root: Vertex) {

  def duplicate: Vertex = ???
}

object Duplicator {
  def apply(v: Vertex): Vertex = {
    new Duplicator(v).duplicate
  }
}
