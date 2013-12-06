package degrel.core

import org.scalatest.FlatSpec


import degrel.utils.FlyWrite._
import degrel.engine.MatchingContext
import degrel.core
import degrel.front
import degrel.front.ParserUtils

class MatchingTest extends FlatSpec {
  val parser = front.DefaultTermParser

  def parse(s: String): core.Vertex = ParserUtils.parseVertex(s)

  it should "match single vertex" in {
    val a = v("foo").matches(v("foo"), MatchingContext.empty)
    assert(a.success)
  }

  it should "not matches different labeled vertex" in {
    val pattern = v("foo")
    val verticies = Seq(v("hoge"),
                         v("bar"),
                         "baz" |^|("piyo", v("hoge")))
    for (v <- verticies) {
      val mch = v.matches(pattern, MatchingContext.empty)
      assert(!mch.success)
    }
  }

  it should "all vertex matches with wildcard vertex" in {
    val pattern = v("*")
    val verticies = Seq(v("foo"),
                         v("bar"),
                         "baz" |^|("foo", v("hoge")))
    for (v <- verticies) {
      val mch = v.matches(pattern, MatchingContext.empty)
      assert(mch.success)
    }
  }

  it should "matches by partial pattern" in {
    val pattern = parse("foo(bar: *)")
    val verticies = Seq("foo(bar: baz)",
                         "foo(hoge: fuga, bar: baz)").map(parse)
    for (v <- verticies) {
      val mch = v.matches(pattern, MatchingContext.empty)
      assert(mch.success)
    }
  }

  it should "reject composite pattern with difference edge name" in {
    val pattern = parse("foo(piyo: *)")
    val verticies = Seq("foo(bar: baz)",
                         "foo(hoge: fuga, bar: baz)").map(parse)
    for (v <- verticies) {
      val mch = v.matches(pattern, MatchingContext.empty)
      assert(!mch.success)
    }
  }

  it should "reject composite pattern with difference child vertex" in {
    val pattern = parse("foo(piyo: baz(hoge: fuga), bar: *)")
    val verticies = Seq("foo(bar: baz, piyo: bazbaz(hoge: fuga))",
                         "foo(hoge: fuga, bar: baz)").map(parse)
    for (v <- verticies) {
      val mch = v.matches(pattern, MatchingContext.empty)
      assert(!mch.success)
    }
  }
}
