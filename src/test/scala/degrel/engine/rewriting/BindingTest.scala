package degrel.engine.rewriting

import degrel.core
import degrel.front.{ParserUtils, TermParser}
import org.scalatest.FlatSpec

class BindingTest extends FlatSpec {
  val parser = TermParser.default

  def parse(s: String): core.Vertex = ParserUtils.parseVertex(s)

  it should "build with multiple vertex" in {
  }
}
