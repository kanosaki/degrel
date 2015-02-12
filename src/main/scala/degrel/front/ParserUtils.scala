package degrel.front

import degrel.core

object ParserUtils {
  private[this] val termParser = TermParser.default
  private[this] val dotlikeParser = dotlike.DigraphParser

  def parseDot(s: String): core.Vertex = {
    dotlikeParser(s).toGraph()
  }

  def parseRule(s: String): core.Rule = {
    degrel.parseVertex(s).asRule
  }

  def parseVertex(s: String): core.Cell = {
    degrel.parseCell(s)
  }
}
