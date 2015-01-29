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


}
