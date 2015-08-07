package degrel.utils.tonberry

import degrel.core

import scala.language.implicitConversions

class VertexExtension(v: core.Vertex) {
  def nextV(expr: String = Query.any): VertexQuery = {
    new VertexQuery(Seq(v), Query.any).nextV(expr)
  }

  def nextE(expr: String = Query.any): EdgeQuery = {
    new EdgeQuery(v.edges, expr)
  }

  def path(expr: String): Query[core.Element] = {
    TPath.select(v, expr)
  }
}
