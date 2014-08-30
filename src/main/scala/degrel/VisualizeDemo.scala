package degrel

object VisualizeDemo {
  val parseV = front.ParserUtils.parseVertex _
  val parseDot = front.ParserUtils.parseDot _

  def main(args: Array[String]): Unit = {
    // Tree
    visualize.show(parseV("a(b: c(foo: bar, hoge: fuga), d: e, f: g(h: i))"))
    // Circular Triangle
    visualize.show(parseDot("@a{-> b: e; b -> c: e; c ->:e}"))
    // Mesh Pentagon
    visualize.show(parseDot(
      """@v1 {
        | -> v2: e
        | -> v3: e
        | -> v4: e
        | -> v5: e
        | v2 ->: e
        | v2 -> v3: e
        | v2 -> v4: e
        | v2 -> v5: e
        | v3 ->: e
        | v3 -> v2: e
        | v3 -> v4: e
        | v3 -> v5: e
        | v4 ->: e
        | v4 -> v2: e
        | v4 -> v3: e
        | v4 -> v5: e
        | v5 ->: e
        | v5 -> v2: e
        | v5 -> v3: e
        | v5 -> v4: e
        |}""".stripMargin))
  }
}
