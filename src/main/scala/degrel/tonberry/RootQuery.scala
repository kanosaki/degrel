package degrel.tonberry

import degrel.core
import degrel.core.Element

class RootQuery(val rootVertex: core.Vertex) extends Query[core.Element] {
  def hasNext: Boolean = false

  def next(): Element = throw new IllegalStateException()

  def nextV(expr: String): VertexQuery = {
    new VertexQuery(Seq(rootVertex), expr)
  }

  def nextE(expr: String): EdgeQuery = {
    this.nextV(Query.any).nextE(expr)
  }

  override def toString = "/"
}
