package degrel.core

import org.scalatest.FlatSpec


import degrel.utils.FlyWrite._
import degrel.engine.MatchingContext
import degrel.core
import degrel.front

class MatchingTest extends FlatSpec {
  val parser = front.DefaultTermParser

  def parseVertex(s: String): core.Vertex = {
    parser(s).root.asInstanceOf[front.AstRoot].toGraph(front.LexicalContext.empty)
  }

  def parseGraph(s: String): core.Vertex = {
    parser(s).root.asInstanceOf[front.AstGraph].roots.head.toGraph(front.LexicalContext.empty)
  }

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
    val pattern = parseGraph("foo(bar: *)")
    val verticies = Seq("foo(bar: baz)",
                         "foo(hoge: fuga, bar: baz)").map(parseGraph)
    for (v <- verticies) {
      val mch = v.matches(pattern, MatchingContext.empty)
      assert(mch.success)
    }
  }

}
