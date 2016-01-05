package degrel.engine.rewriting

import degrel.core.{Vertex, VertexBody, VertexHeader}
import degrel.engine.LocalDriver

/**
 * @param target target vertex
 * @param root root of target vertex
 * @param self owner of target vertex
 */
case class RewritingTarget(target: VertexHeader, root: VertexHeader, self: LocalDriver) extends VertexHeader(target.id) {
  override def body: VertexBody = target.body

  override def write(v: Vertex): Unit = target.write(v)

  override def shallowCopy(): Vertex = RewritingTarget(target, root, self)
}

object RewritingTarget {
  def alone(v: Vertex, self: LocalDriver): RewritingTarget = {
    RewritingTarget(v.asHeader, v.asHeader, self)
  }

}
