package degrel.engine.rewriting

import degrel.core.{VertexBody, Vertex, VertexHeader}
import degrel.engine.{Driver, LocalDriver}

/**
 * @param target target vertex
 * @param root root of target vertex
 * @param self owner of target vertex
 */
case class RewritingTarget(target: VertexHeader, root: VertexHeader, self: Driver) extends VertexHeader {
  override def body: VertexBody = target.body

  override def write(v: Vertex): Unit = target.write(v)

  override def shallowCopy(): Vertex = RewritingTarget(target, root, self)
}

object RewritingTarget {
  def alone(v: Vertex, self: Driver = LocalDriver()): RewritingTarget = {
    RewritingTarget(v.asHeader, v.asHeader, self)
  }

}
