package degrel.tonberry

import scala.language.implicitConversions
import degrel.core

class VertexExtension(v: core.Vertex) {
  def nextV(expr: String = Query.any) : VertexQuery = {
    new VertexQuery(Seq(v), Query.any).nextV(expr)
  }

  def nextE(expr: String = Query.any) : EdgeQuery = {
    new EdgeQuery(v.edges(), expr)
  }

  def path(expr: String) : Query[core.Element] = {
    var ret : Query[core.Element] = new VertexQuery(Seq(v), Query.any)
    for(block <- expr.split('/').filter(_ != "")) {
      block.split(":", 2) match {
        case Array(vExpr) => {
          if(vExpr != "")
            ret = ret.nextV(vExpr)
        }
        case Array(vExpr, eExpr) => {
          if(vExpr != "")
            ret = ret.nextV(vExpr)
          if(eExpr != "")
            ret = ret.nextE(eExpr)
        }
        case _ => throw new Exception("Invalid path")
      }
    }
    if(ret == null){
      throw new Exception("Invalid path")
    } else {
      ret
    }
  }
}
