package degrel.core

import org.scalatest.FlatSpec
import degrel.utils.TestUtils._

class VertexTest extends FlatSpec {
  def parse(s: String) = degrel.parseVertex(s)

  "thruSingle" should "return next vertex with specified label" in {
    val v = parse("foo@X(x: bar, y: baz, z: X)")
    assert(v.thruSingle("x") ===~ parse("bar"))
  }

  it should "throw error if there is no matching labeled edge" in {
    val v = parse("foo@X(x: bar, y: baz, z: X)")
    intercept[Exception] {
      v.thruSingle("hoge")
    }
  }

  it should "throw error if there is poly edge with specified label" in {
    val v = parse("foo@X(x: bar, x: baz, z: X)")
    intercept[Exception] {
      v.thruSingle("x")
    }
  }

  //"asCell" should "return a vertex as `Cell` instance" in {
  //  val v = parse("__cell__(__item__: hoge)")
  //  val expected = parse("{hoge}")
  //  val actual = v.asCell
  //  assert(actual ===~ expected)
  //  assert(!v.isInstanceOf[Cell])
  //  assert(actual.isInstanceOf[Cell])
  //}
}
