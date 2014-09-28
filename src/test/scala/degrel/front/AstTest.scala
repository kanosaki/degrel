package degrel.front

import degrel.Query._
import degrel.core
import degrel.core.{Label, Vertex}
import degrel.utils.FlyWrite._
import degrel.utils.TestUtils._
import org.scalatest.FlatSpec

class AstTest extends FlatSpec {
  val parser = TermParser.default
  val SE = SpecialLabel.Edge
  val SV = SpecialLabel.Vertex

  def parseFirstRoot(expr: String): AstGraph = {
    parser(expr).root
  }

  def assertAst(astRoot: AstGraph, root: core.Vertex, context: LexicalContext = LexicalContext.empty) {
    val graph = astRoot.toGraph(context)
    assert(graph ===~ root)
  }

  it should "construct simple rule" in {
    val v = parseFirstRoot("foo -> bar")
    val expected = "foo".v |->| "bar".v
    assertAst(v, expected)
  }

  it should "construct single vertex" in {
    val v = parseFirstRoot("foo")
    assertAst(v, "foo".v)
  }

  it should "construct a vertex and its edges" in {
    val v = parseFirstRoot("foo(bar: baz)")
    assertAst(v, "foo" |^| ("bar" |:| "baz".v))
  }

  it should "construct multi vertex" in {
    val v = parseFirstRoot("foo(bar: hoge(fuga: piyo), baz: foobar)")
    val eBar = "bar" |:| ("hoge" |^| ("fuga" |:| "piyo".v))
    val eBaz = "baz" |:| "foobar".v
    assertAst(v, "foo" |^|(eBar, eBaz))
  }

  it should "construct a litte complex rule" in {
    val v = parseFirstRoot("foo(bar: baz) -> bar(baz: foo)")
    val vLhs = "foo" |^| ("bar" |:| "baz".v)
    val vRhs = "bar" |^| ("baz" |:| "foo".v)
    assertAst(v, vLhs |->| vRhs)
  }

  it should "capture no variables when graph as no capture variable" in {
    val v = parseFirstRoot("foo(bar: baz(hoge: fuga))").asInstanceOf[AstVertex]
    assert(captureAndRetAsSet(v) === Map())
  }

  def captureAndRetAsSet(v: AstVertex): Map[String, Vertex] = {
    val context = new LhsContext(parent = LexicalContext.empty)
    v.capture(context)
    context.toSymMap.toMap
  }

  def assertCaptures(cap1: Map[String, Vertex], cap2: Map[String, Vertex]) = {
    assert(cap1.keySet === cap2.keySet)
    cap1.foreach(
    {
      case (capvar, v1) => {
        assert(v1 ===~ cap2(capvar))
      }
    })
  }

  it should "capture a variable from root vertex" in {
    val v = parseFirstRoot("A[foo](bar: baz)").asInstanceOf[AstVertex]
    assertCaptures(captureAndRetAsSet(v),
      Map("A" -> ("foo" |^| ("bar" |:| "baz".v))))
  }

  it should "capture a variable from other vertex" in {
    val v = parseFirstRoot("hoge(fuga: A[foo](bar: baz))").asInstanceOf[AstVertex]
    assertCaptures(captureAndRetAsSet(v),
      Map("A" -> ("foo" |^| ("bar" |:| "baz".v))))
  }

  it should "capture multiple variables" in {
    val v = parseFirstRoot("hoge(fuga: A[foo](bar: baz), piyo: B[bar](baz: foo), foobar: C[baz](foo: bar))")
      .asInstanceOf[AstVertex]
    assertCaptures(captureAndRetAsSet(v),
      Map("A" -> ("foo" |^| ("bar" |:| "baz".v)),
        "B" -> ("bar" |^| ("baz" |:| "foo".v)),
        "C" -> ("baz" |^| ("foo" |:| "bar".v))))
  }

  it should "capture a variable in rule" in {
    val v = parseFirstRoot("hoge(fuga: A[foo](bar: baz)) -> foobar(baz: A)").asInstanceOf[AstRule]
    val graph = v.toGraph(LexicalContext.empty)
    val rhs = graph.edges(SE.rhs).head.dst
    assert(rhs.label === Label("foobar"))
    val expectedCapturedV = "foo" |^| ("bar" |:| "baz".v)
    val expectedValue = "foobar" |^| ("baz" |:| core
      .Vertex("@", Seq(core.Edge(null, core.Label("_ref"), expectedCapturedV))))
    assert(rhs ===~ expectedValue)
  }

  it should "captured vertex has same reference" in {
    val v = parseFirstRoot("A[foo](bar: baz) -> A").asInstanceOf[AstRule]
    val graph = v.toGraph(LexicalContext.empty)
    val rhs = graph.edges(SE.rhs).head.dst
    val lhs = graph.edges(SE.lhs).head.dst
    val captured = rhs.edges(SE.ref).head.dst
    assert(captured eq lhs, s"$captured dit not eq $lhs")
  }

  def mkRefVertex(refTo: core.Vertex): core.Vertex = {
    "@" |^| ("_ref" |:| refTo)
  }

  it should "concrete complex graph" in {
    val v = parseFirstRoot("hoge(fuga: A[foo](bar: baz), piyo: B) -> x(y: A, z: B)").asInstanceOf[AstRule]
    val actualGraph = v.toGraph(LexicalContext.empty)
    val capAsA = "foo" |^| ("bar" |:| "baz".v)
    val capAsB = "*".v
    val expectedLhs = "hoge" |^|("fuga" |:| capAsA, "piyo" |:| capAsB)
    val expectedRhs = "x" |^|("y" |:| mkRefVertex(capAsA), "z" |:| mkRefVertex(capAsB))
    val expectedGraph = expectedLhs |->| expectedRhs
    assert(expectedGraph ===~ actualGraph)
    assert(actualGraph.path(":_lhs/hoge/foo").exact eq actualGraph.path(":_rhs/x:y/@/*").exact)
    assert(actualGraph.path("/->:_lhs/hoge:piyo/*").exact eq actualGraph.path("/->:_rhs/x:z/@/*").exact)
  }
}
