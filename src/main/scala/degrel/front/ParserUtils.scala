package degrel.front

import degrel.core

object ParserUtils {
  private val termParser = TermParser.default
  private val dotlikeParser = dotlike.DigraphParser

  def parseDot(s: String): core.Vertex = {
    dotlikeParser(s).toGraph()
  }

  def parseRule(s: String): core.Rule = {
    this.parseVertex(s).asInstanceOf[core.Rule]
  }

  def parseVertex(s: String): core.Vertex = {
    termParser(s).toGraph()
  }
}
