package degrel.tonberry

import scala.util.matching.Regex

trait Query[+E] extends Iterator[E] {
  def nextV(expr: String = Query.any): VertexQuery

  def nextE(expr: String = Query.any): EdgeQuery

  def exact: E = {
    if (this.hasNext) {
      val ret = this.next()
      if (this.hasNext) {
        throw new QueryException(s"This query(${this.toString()}) has two or more results.")
      }
      ret
    } else {
      throw new QueryException(s"This query(${this.toString()}) has no result.")
    }
  }

  def mkPattern(expr: String): Regex = {
    expr.replace("*", ".*?").r
  }

}

object Query {
  val any: String = "*"
}

class QueryException(msg: String) extends Exception(msg) {

}
