package degrel.utils.tonberry

import degrel.core
import degrel.core.Element

import scala.util.parsing.combinator.RegexParsers

object TPath {
  def select(v: core.Vertex, expr: String) = {
    TPathParser.query(v, expr)
  }
}

trait AstQuery {
  def chain(prev: Query[core.Element]): Query[core.Element]
}

case class AstEdgeQuery(expr: String) extends AstQuery {
  def chain(prev: Query[Element]): Query[Element] = {
    prev.nextE(expr)
  }
}

case class AstVertexQuery(expr: String) extends AstQuery {
  def chain(prev: Query[Element]): Query[Element] = {
    prev.nextV(expr)
  }
}

object TPathParser extends RegexParsers {
  val eSep = """(?!\\):""".r
  val vSep = """(?!\\)/""".r

  val pat_label = """[^:/]+""".r

  val vBlock =
    eSep ~> pat_label ^^ (s => Seq(AstEdgeQuery(s))) |
      pat_label ~ opt(eSep ~ pat_label) ^^ {
        case vl ~ Some(_ ~ el) => Seq(AstVertexQuery(vl), AstEdgeQuery(el))
        case vl ~ None => Seq(AstVertexQuery(vl))
      }

  val path = opt("/") ~ repsep(vBlock, vSep) ^^ {
    case Some(_) ~ blocks => (true, blocks.flatten)
    case None ~ blocks => (false, blocks.flatten)
  }

  def query(v: core.Vertex, expr: String): Query[Element] = {
    parseAll(path, expr) match {
      case Success((true, blocks), _) => {
        // Absolute query
        val init = new RootQuery(v).asInstanceOf[Query[Element]]
        blocks.foldLeft(init)((q, ast) => ast.chain(q))
      }
      case Success((false, blocks), _) => {
        // Relative query
        val init = new VertexQuery(Seq(v), Query.any).asInstanceOf[Query[Element]]
        blocks.foldLeft(init)((q, ast) => ast.chain(q))
      }
      case fail: NoSuccess =>
        throw new TPathSyntaxError(fail.msg + s" in '$expr'")
    }
  }
}

class TPathSyntaxError(msg: String) extends TonberryException(msg: String) {

}
