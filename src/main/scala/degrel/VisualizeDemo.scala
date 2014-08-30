package degrel

object VisualizeDemo {
  val parseV = front.ParserUtils.parseVertex _
  val parseDot = front.ParserUtils.parseDot _

  def main(args: Array[String]): Unit = {
    visualize.show(parseV("a(b: c(foo: bar, hoge: fuga), d: e, f: g(h: i))"))
    visualize.show(parseDot(
      """@a{ -> b: e; b -> c: e; c -> : e}"""))
  }
}
