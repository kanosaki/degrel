package degrel.tonberry

import degrel.core

class EdgeQuery(val src: Iterable[core.Edge], val expr: String) extends Query[core.Edge] {
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
    new VertexQuery(matched.map(_.dst), expr)
  }

  def nextE(expr: String = Query.any): EdgeQuery = {
    this.nextV(Query.any).nextE(expr)
  }
}
