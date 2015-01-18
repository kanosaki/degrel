package degrel.engine

import akka.pattern.ask
import akka.util.Timeout
import degrel.core
import degrel.front.{ParserUtils, TermParser}
import degrel.engine.rewriting.LocalReserve
import degrel.utils.TestUtils.assertElementSet
import org.scalatest.FlatSpec

import scala.concurrent.Await
import scala.concurrent.duration._

class ParallelRewriteTest extends FlatSpec {
  val parser = TermParser.default

  def parse(s: String): core.Vertex = ParserUtils.parseVertex(s)

  def parseR(s: String): core.Rule = ParserUtils.parseRule(s)

  implicit val timeout = Timeout(2.seconds)

  it should "not stop until rewriting will have been completely stopped" in {
  }
}
