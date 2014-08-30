package degrel.tonberry

import degrel.core
import degrel.core.Vertex

class VertexQuery(val src: Iterable[core.Vertex], val expr: String, val parent: Query[core.Element] = null) extends Query[core.Vertex] {
  lazy val matched = src.filter(v => v.label.expr match {
    case pattern() => true
    case _ => false
  })
  lazy val matchedItor = matched.iterator
  val pattern = this.mkPattern(expr)

  def hasNext: Boolean = matchedItor.hasNext

  def next(): Vertex = matchedItor.next()

  def nextV(expr: String = Query.any): VertexQuery = {
    this.nextE(Query.any).nextV(expr)
  }

  def nextE(expr: String = Query.any): EdgeQuery = {
    new EdgeQuery(matched.flatMap(_.edges()), expr, this)
  }

  def freeze: VertexQuery = {
    new VertexQuery(src.map(_.freeze), expr, this)
  }

  override def toString = {
    val parentStr = parent match {
      case null => ""
      case _ => parent.toString()
    }
    parentStr + expr
  }
}
