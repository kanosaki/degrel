package degrel.front

import org.parboiled2.{ErrorFormatter, ParseError}

import scala.util.{Failure, Success, Try}

object Parser {
  val current: ParserImpl = Parboiled

  def cell(s: String): AstCell = {
    current.cell(s)
  }

  def vertex(s: String): AstVertex = {
    current.vertex(s)
  }

  trait ParserImpl {
    def vertex(s: String): AstVertex

    def cell(s: String): AstCell
  }


  object Combinator extends ParserImpl {
    def vertex(s: String) = TermParser.parseExpr(s)

    def cell(s: String) = TermParser.parseCell(s)
  }

  object Parboiled extends ParserImpl {
    def handleError[T](parser: ParboiledParser, res: Try[T]): T = res match {
      case Success(res) => res
      case Failure(th) => {
        th match {
          case pe: ParseError => {
            val errMsg = parser.formatError(pe, new ErrorFormatter(showTraces = true))
            throw new SyntaxError(errMsg)
          }
          case _ => throw th
        }
      }
    }

    def vertex(s: String): AstVertex = {
      val parser = new ParboiledParser(s)
      val result = parser.allAsExpression.run()
      handleError(parser, result)
    }

    def cell(s: String): AstCell = {
      val parser = new ParboiledParser(s)
      val result = parser.allAsCell.run()
      handleError(parser, result)
    }
  }

}


