package degrel

object VisualizeDemo {
  def main(args: Array[String]) = {
    val vs = visualize.VisualizeService
    val parseV = front.ParserUtils.parseVertex _
    vs.show(parseV("a(b: c, d: e)"))
  }
}
