package degrel.engine

import org.scalatest.FlatSpec
import degrel.rewriting.LocalReserve
import degrel.front
import degrel.front.ParserUtils
import degrel.core
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await

class ParallelRewriteTest extends FlatSpec {
  val parser = front.DefaultTermParser

  def parse(s: String): core.Vertex = ParserUtils.parseVertex(s)

  def parseR(s: String): core.Rule = ParserUtils.parseRule(s)
  implicit val timeout = Timeout(10.seconds)

  it should "not stop until rewriting will have been completely stopped" in {
    val reserve = new LocalReserve()
    reserve.addRule(parseR("a -> b"))
    reserve.addRule(parseR("b -> c"))
    reserve.addRule(parseR("c -> d"))
    reserve.addVertex(parse("a"))
    val scheduler = RewriteScheduler(reserve)
    Await.result(scheduler ? RewriteScheduler.Run, timeout.duration)
    val expected = Set(parse("d")).map(_.freeze)
    val actual = reserve.freeze.roots.toSet
    assert(expected === actual)
  }
}
