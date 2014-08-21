package degrel

object VisualizeDemo {
  def main(args: Array[String]): Unit = {
    val vs = visualize.VisualizeService
    val parseV = front.ParserUtils.parseVertex _
    val parseDot = front.ParserUtils.parseDot _
    vs.show(parseV("a(b: c(foo: bar, hoge: fuga), d: e, f: g(h: i))"))
    vs.show(parseDot("@a{ -> b: e; b -> c: e; c -> : e}"))
  }
}
