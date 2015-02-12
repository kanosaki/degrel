package degrel.dgspec

import degrel.core.Vertex


class SpecContext {
  var root: Vertex = null
}

object SpecContext {
  def empty() = {
    new SpecContext()
  }
}