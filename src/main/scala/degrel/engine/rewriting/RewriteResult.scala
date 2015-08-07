package degrel.engine.rewriting

import degrel.core.{Rule, Vertex, VertexHeader}
import degrel.engine.Driver

trait RewriteResult {
  def done: Boolean

  def exec(self: Driver) = {}
}

object RewriteResult {
  def write(target: VertexHeader, value: Vertex): RewriteResult = {
    Write(target, value)
  }

  def continue(target: VertexHeader, rule: Rule, binding: Binding): RewriteResult = {
    Continue(target, rule, binding)
  }

  case class Write(target: VertexHeader, value: Vertex) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: Driver): Unit = {
      self.writeVertex(target, value)
    }
  }

  case class Continue(target: VertexHeader, rule: Rule, binding: Binding) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: Driver): Unit = {
      self.writeVertex(target, rule)
      val rw = new ContinueRewriter(rule, binding, target)
      self.addContinueRewriter(rw)
    }
  }


  case object Nop extends RewriteResult {
    def done = false
  }

  case object Done extends RewriteResult {
    def done = true
  }

}

