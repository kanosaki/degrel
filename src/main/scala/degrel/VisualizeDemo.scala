package degrel

object VisualizeDemo {
  def main(args: Array[String]): Unit = {
    val vs = visualize.VisualizeService
    val parseV = front.ParserUtils.parseVertex _
    vs.show(parseV("a(b: c(foo: bar, hoge: fuga), d: e, f: g(h: i))"))
  }
}
