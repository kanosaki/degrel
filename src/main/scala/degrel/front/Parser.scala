package degrel.front

import scala.util.parsing.combinator.RegexParsers

class SyntaxError(msg: String) extends Exception {
  override def toString = {
    msg
  }
}

object RegrelParser {
}

object DefaultTermParser extends RegexParsers {
  def capture: Parser[AstCapture] = """[A-Z][a-zA-Z0-9]*""".r ^^ {
    AstCapture(_)
  }

  def label: Parser[AstLabel] =
    """[_~=.+\-*a-z0-9][_~=.+\-*a-z0-9A-Z]*""".r ^^ {
      AstLabel(_)
    }

  def name: Parser[AstName] =
    (capture ~ opt("[" ~> label <~ "]")) ^^ {
      case cap ~ lbl => AstName(Some(cap), lbl)
    } |
      label ^^ {
        case l => AstName(None, Some(l))
      }

  def edge: Parser[AstEdge] = label ~ ":" ~ vertex ^^ {
    case n ~ _ ~ v => AstEdge(n, v)
  }

  def edges: Parser[Seq[AstEdge]] = "(" ~> repsep(edge, ",") <~ ")" ^^ {
    _.toSeq
  }

  def vertex: Parser[AstVertex] = name ~ opt(edges) ^^ {
    case n ~ Some(es) => AstVertex(n, es)
    case n ~ None => AstVertex(n, Seq())
  }

  def root: Parser[AstRoot] = "(" ~> root <~ ")" | vertex ~ opt(rule_) ^^ {
    case v ~ None => v
    case v ~ Some(r) => AstRule(v, r)
  }

  def rule_ : Parser[AstRoot] = "->" ~> root

  def graph: Parser[AstGraph] = root ^^ {
    case r => AstGraph(Seq(r))
  } | "(" ~> repsep(root, ",") <~ ")" ^^ {
    case rs => AstGraph(rs)
  }

  def apply(expr: String): Ast = {
    parseAll(graph, expr) match {
      case Success(gr, _) => new Ast(gr)
      case fail: NoSuccess => {
        throw new SyntaxError(fail.msg)
      }
    }
  }
}

