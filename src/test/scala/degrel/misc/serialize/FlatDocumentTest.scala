package degrel.misc.serialize

import org.scalatest.FlatSpec

class FlatDocumentTest extends FlatSpec {
  def parse = degrel.parseVertex _
  def trim = scala.xml.Utility.trim _

  "For single vertex" should "return in XML" in {
    val graph = parse("hoge").toGraph
    val expected =
      <graph>
        <vertex id="0" label="hoge">
        </vertex>
      </graph>
    val doc = toDoc(graph, FormatFlavor.Flat)
    val actual = new XmlProvider().dump(doc)
    assert(trim(expected) === trim(actual))
  }

  "For simple functor" should "return in XML" in {
    val graph = parse("a(b: c, d: e)").toGraph
    val expected =
      <graph>
        <vertex id="0" label="a">
          <edge label="b" ref="1"></edge>
          <edge label="d" ref="2"></edge>
        </vertex>
        <vertex id="1" label="c">
        </vertex>
        <vertex id="2" label="e">
        </vertex>
      </graph>
    val doc = toDoc(graph, FormatFlavor.Flat)
    val actual = new XmlProvider().dump(doc)
    assert(trim(expected) === trim(actual))
  }

  "For simple graph" should "return in XML" in {
    val graph = parse("a@X(b: b(a: X))").toGraph
    val expected =
      <graph>
        <vertex id="0" label="a">
          <edge label="b" ref="1"></edge>
        </vertex>
        <vertex id="1" label="b">
          <edge label="a" ref="0"></edge>
        </vertex>
      </graph>
    val doc = toDoc(graph, FormatFlavor.Flat)
    val actual = new XmlProvider().dump(doc)
    assert(trim(expected) === trim(actual))
  }

  "A rule" should "return in XML" in {
    val graph = parse("a(b: c@C) -> x(y: z, p: C)").toGraph
    val expected =
      <graph>
        <vertex id="0" label="->">
          <edge label="__lhs__" ref="1"></edge>
          <edge label="__rhs__" ref="2"></edge>
        </vertex>
        <vertex id="1" label="a">
          <edge label="b" ref="3"></edge>
        </vertex>
        <vertex id="2" label="x">
          <edge label="y" ref="4"></edge>
          <edge label="p" ref="5"></edge>
        </vertex>
        <vertex id="3" label="c">
        </vertex>
        <vertex id="4" label="z">
        </vertex>
        <vertex id="5" label="__ref__">
          <edge label="__to__" ref="3"></edge>
        </vertex>
      </graph>
    val doc = toDoc(graph, FormatFlavor.Flat)
    val actual = new XmlProvider().dump(doc)
    assert(trim(expected) === trim(actual))
  }
}
