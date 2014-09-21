package degrel.tonberry

import degrel.core.{Edge, Vertex}

import scala.util.matching.Regex

trait Query[+E] extends Iterator[E] {
  def nextV(expr: String = Query.any): VertexQuery

  def nextE(expr: String = Query.any): EdgeQuery

  def exactAs[T]: T = {
    this.exact.asInstanceOf[T]
  }

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

  def singleV: Vertex = {
    this.exact match {
      case v: Vertex => v
      case e: Edge => e.dst
    }
  }

  def mkPattern(expr: String): Regex = {
    expr.replace("*", ".*?").r
  }

}

object Query {
  val any: String = "*"
}

class QueryException(msg: String) extends TonberryException(msg) {

}
