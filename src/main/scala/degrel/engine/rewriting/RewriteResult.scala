package degrel.engine.rewriting

import degrel.core._
import degrel.engine.LocalDriver

import scala.concurrent.Await
import scala.concurrent.duration._

trait RewriteResult {
  def done: Boolean

  def exec(self: LocalDriver) = {}
}

object RewriteResult {

  case class Write(target: VertexHeader, value: Vertex) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: LocalDriver): Unit = {
      self.writeVertex(target, value)
    }
  }

  case class Continue(target: RewritingTarget, rule: Rule, binding: Binding) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: LocalDriver): Unit = {
      self.writeVertex(target, rule)
      val rw = new ContinueRewriter(rule, binding, target.target)
      self.addContinueRewriter(rw)
    }
  }

  case class AddRoot(target: VertexHeader, value: Vertex) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: LocalDriver): Unit = {
      Await.result(self.dispatch(target, value), 10.seconds)
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

    override def exec(self: LocalDriver): Unit = {
      this.results.foreach(_.exec(self))
    }
  }

  case class IO(action: LocalDriver => Unit) extends RewriteResult {
    override def done: Boolean = true

    override def exec(self: LocalDriver): Unit = {
      action(self)
    }
  }


  case object Nop extends RewriteResult {
    def done = false
  }

  case object Done extends RewriteResult {
    def done = true
  }

}

