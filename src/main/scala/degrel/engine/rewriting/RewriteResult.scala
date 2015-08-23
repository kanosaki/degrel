package degrel.engine.rewriting

import degrel.core.{Cell, Rule, Vertex}
import degrel.engine.Driver

trait RewriteResult {
  def done: Boolean

  def exec(self: Driver) = {}
}

object RewriteResult {

  case class Write(target: RewritingTarget, value: Vertex) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: Driver): Unit = {
      self.writeVertex(target, value)
    }
  }

  case class Continue(target: RewritingTarget, rule: Rule, binding: Binding) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: Driver): Unit = {
      self.writeVertex(target, rule)
      val rw = new ContinueRewriter(rule, binding, target.target)
      self.addContinueRewriter(rw)
    }
  }

  case class AddRoot(target: Cell, value: Vertex) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: Driver): Unit = {
      self.addRoot(target, value)
    }
  }

  case class Multi(results: Seq[RewriteResult]) extends RewriteResult {
    override def done: Boolean = {
      val allTrue = results.forall(_.done)
      val allFalse = results.forall(_.done)
      if (allTrue || allFalse) {
        allTrue
      } else {
        throw new RuntimeException("Inconsistent result")
      }
    }

    override def exec(self: Driver): Unit = {
      this.results.foreach(_.exec(self))
    }
  }


  case object Nop extends RewriteResult {
    def done = false
  }

  case object Done extends RewriteResult {
    def done = true
  }

}

