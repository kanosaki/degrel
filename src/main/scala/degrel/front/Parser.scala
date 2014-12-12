package degrel.front

object Parser {
  def parse(s: String): AstNode = {
    ???
  }

  def cell(s: String): AstCell = {
    ???
  }

  def vertex(s: String): AstVertex = {
    TermParser.parseExpr(s)
  }
}


