package degrel.tonberry

import degrel.core

class EdgeQuery(val src: Iterable[core.Edge], val expr: String, val parent: Query[core.Element] = null) extends Query[core.Edge] {
  private lazy val pattern = this.mkPattern(expr)

  private lazy val matched = src.filter(e => e.label.expr match {
    case pattern() => true
    case _ => false
  })
  private lazy val matchedItor = matched.iterator

  def hasNext: Boolean = matchedItor.hasNext

  def next(): core.Edge = {
    matchedItor.next()
  }

  def dst: VertexQuery = {
    this.nextV(Query.any)
  }

  def nextV(expr: String = Query.any): VertexQuery = {
    new VertexQuery(matched.map(_.dst), expr, this)
  }

  def nextE(expr: String = Query.any): EdgeQuery = {
    this.nextV(Query.any).nextE(expr)
  }

  def freeze: EdgeQuery = {
    new EdgeQuery(src.map(_.freeze), expr, this)
  }

  override def toString = {
    val parentStr = parent match {
      case null => ""
      case _ => parent.toString()
    }
    parentStr + s":$expr/"
  }
}
