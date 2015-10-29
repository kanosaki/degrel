package degrel.engine.rewriting

import degrel.core.{Vertex, VertexHeader}
import degrel.engine.{LocalDriver, Driver, LocalDriver$}

trait RewriterOp {
  def apply(driver: Driver)
}

object RewriterOp {
  def write(target: VertexHeader, value: Vertex): RewriterOp = new Write(target, value)
  val nop = Nop

  class Write(target: VertexHeader, value: Vertex) extends RewriterOp {
    override def apply(driver: Driver): Unit = {
      target.write(value)
    }
  }


  object Nop extends RewriterOp {
    override def apply(driver: Driver): Unit = {}
  }

}
