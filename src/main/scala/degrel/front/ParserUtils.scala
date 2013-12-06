package degrel.front

import degrel.core

object ParserUtils {
  private val termParser = DefaultTermParser

  def parseVertex(s: String): core.Vertex = {
    termParser(s).toGraph()
  }
}
