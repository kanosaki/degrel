package degrel

import degrel.front.ParserUtils
import degrel.engine.MatchingContext

object Main {

  def main(args: Array[String]) = {
    //val graph = ParserUtils.parseVertex("foo(bar: baz(hoge: fuga), piyo: joe)")
    //val pattern = ParserUtils.parseVertex("foo(bar: baz, piyo: *)")
    //val res = graph.matches(pattern, MatchingContext.empty)
    val graph = ParserUtils.parseVertex("foo(bar: baz)")
    val pattern = ParserUtils.parseVertex("foo(piyo: *)")
    val res = graph.matches(pattern, MatchingContext.empty)
    println(res)
  }
}
