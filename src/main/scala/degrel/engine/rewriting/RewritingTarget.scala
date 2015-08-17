package degrel.engine.rewriting

import degrel.core.{Vertex, VertexHeader}
import degrel.engine.Driver

/**
 * @param target target vertex
 * @param root root of target vertex
 * @param self owner of target vertex
 */
case class RewritingTarget(target: VertexHeader, root: VertexHeader, self: Driver) {

}

object RewritingTarget {
  def alone(v: Vertex, self: Driver = Driver()): RewritingTarget = {
    RewritingTarget(v.asHeader, v.asHeader, self)
  }

}
