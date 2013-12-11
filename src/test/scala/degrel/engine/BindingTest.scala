package degrel.engine

import org.scalatest.FlatSpec
import degrel.front
import degrel.core
import degrel.front.ParserUtils

class BindingTest extends FlatSpec {
  val parser = front.DefaultTermParser

  def parse(s: String): core.Vertex = ParserUtils.parseVertex(s)

  it should "build with multiple vertex" in {
    val dataV = parse("foo(bar: baz, x: a(b: c(d: e)))")
    val pattern = parse("foo(bar: *, x: *)")
    val mch = dataV.matches(pattern)
  }
}
