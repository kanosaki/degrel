package degrel.dgspec

import degrel.core.Vertex
import degrel.engine.Chassis


class SpecContext(var chassis: Chassis) {
  var root: Vertex = null
  var lastOutput: String = null
}

object SpecContext {
  def empty() = {
    new SpecContext(Chassis.create())
  }

  def default() = {
    new SpecContext(Chassis.createWithMain())
  }
}