package degrel.engine

import akka.pattern.ask
import akka.util.Timeout
import degrel.core
import degrel.front.{ParserUtils, TermParser}
import degrel.rewriting.LocalReserve
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
    val reserve = new LocalReserve()
    reserve.addRule(parseR("a -> b"))
    reserve.addRule(parseR("b -> c"))
    reserve.addRule(parseR("c -> d"))
    reserve.addRule(parseR("d -> e"))
    reserve.addVertex(parse("a"))
    reserve.addVertex(parse("a"))
    reserve.addVertex(parse("b"))
    reserve.addVertex(parse("c"))
    reserve.addVertex(parse("d"))
    val scheduler = RewriteScheduler(reserve)
    Await.result(scheduler ? RewriteScheduler.Run, timeout.duration)
    val expected = Set(parse("e"), parse("e"), parse("e"), parse("e"), parse("e"))
    val actual = reserve.roots.toSet
    assertElementSet(expected, actual)
  }
}
