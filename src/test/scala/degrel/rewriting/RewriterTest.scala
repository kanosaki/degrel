package degrel.rewriting

import degrel.core
import degrel.front.{ParserUtils, TermParser}
import org.scalatest.FlatSpec

class RewriterTest extends FlatSpec {
  val parser = TermParser.default

  def parse(s: String): core.Vertex = ParserUtils.parseVertex(s)

  def parseR(s: String): core.Rule = ParserUtils.parseRule(s)

  def parseDot(s: String): core.Graph = ParserUtils.parseDot(s).toGraph

}
