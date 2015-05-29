package degrel.dgspec

import degrel.core.Vertex
import degrel.engine.Chassis


class SpecContext(val chassis: Chassis) {
  var root: Vertex = null
}

object SpecContext {
  def empty() = {
    new SpecContext(Chassis.create())
  }

  def default() = {
    new SpecContext(Chassis.createWithMain())
  }
}