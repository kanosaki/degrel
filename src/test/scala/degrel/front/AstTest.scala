package degrel.front

import org.scalatest.FlatSpec

import degrel.core
import degrel.core.Label
import degrel.utils.FlyWrite._
import degrel.Query._

class AstTest extends FlatSpec {
  val parser = DefaultTermParser
  val SE = SpecialLabel.Edge
  val SV = SpecialLabel.Vertex

  def parseFirstRoot(expr: String): AstRoot = {
    val ast = parser(expr)
    ast.root match {
      case AstGraph(root :: _) => root
      case _ => throw new Exception("Unsupported syntax!")
    }
  }

  def assertAst(astRoot: AstRoot, root: core.Vertex, context: LexicalContext = LexicalContext.empty) {
    assert(astRoot.toGraph(context) === root)
  }

  it should "construct single vertex" in {
    val v = parseFirstRoot("foo")
    assertAst(v, ("foo" |^|()))
  }

  it should "construct a vertex and its edges" in {
    val v = parseFirstRoot("foo(bar: baz)")
    assertAst(v, "foo" |^| ("bar" |:| ("baz" |^|())))
  }

  it should "construct multi vertex" in {
    val v = parseFirstRoot("foo(bar: hoge(fuga: piyo), baz: foobar)")
    val eBar = "bar" |:| ("hoge" |^| ("fuga" |:| ("piyo" |^|())))
    val eBaz = "baz" |:| ("foobar" |^|())
    assertAst(v, "foo" |^|(eBar, eBaz))
  }

  it should "construct simple rule" in {
    val v = parseFirstRoot("foo -> bar")
    assertAst(v,
              ("foo" |^|()) |->| ("bar" |^|()))
  }

  it should "construct a litte complex rule" in {
    val v = parseFirstRoot("foo(bar: baz) -> bar(baz: foo)")
    val vLhs = "foo" |^| ("bar" |:| ("baz" |^|()))
    val vRhs = "bar" |^| ("baz" |:| ("foo" |^|()))
    assertAst(v, vLhs |->| vRhs)
  }

  it should "capture no variables when graph as no capture variable" in {
    val v = parseFirstRoot("foo(bar: baz(hoge: fuga))").asInstanceOf[AstVertex]
    assert(v.capture(LexicalContext.empty) === Nil)
  }

  it should "capture a variable from root vertex" in {
    val v = parseFirstRoot("A[foo](bar: baz)").asInstanceOf[AstVertex]
    assert(v.capture(new LhsContext(parent = LexicalContext.empty))
           === List(("A", "foo" |^| ("bar" |:| ("baz" |^|())))))
  }

  it should "capture a variable from other vertex" in {
    val v = parseFirstRoot("hoge(fuga: A[foo](bar: baz))").asInstanceOf[AstVertex]
    assert(v.capture(new LhsContext(parent = LexicalContext.empty)).toSet
           === Set(("A", "foo" |^| ("bar" |:| ("baz" |^|())))))
  }

  it should "capture multiple variables" in {
    val v = parseFirstRoot("hoge(fuga: A[foo](bar: baz), piyo: B[bar](baz: foo), foobar: C[baz](foo: bar))")
      .asInstanceOf[AstVertex]
    assert(v.capture(new LhsContext(parent = LexicalContext.empty)).toSet
           === Set(("A", "foo" |^| ("bar" |:| ("baz" |^|()))),
                   ("B", "bar" |^| ("baz" |:| ("foo" |^|()))),
                   ("C", "baz" |^| ("foo" |:| ("bar" |^|())))))
  }

  it should "capture a variable in rule" in {
    val v = parseFirstRoot("hoge(fuga: A[foo](bar: baz)) -> foobar(baz: A)").asInstanceOf[AstRule]
    val graph = v.toGraph(LexicalContext.empty)
    val rhs = graph.edges(SE.rhs).head.dst
    assert(rhs.label === Label("foobar"))
    val expectedCapturedV = "foo" |^| ("bar" |:| ("baz" |^|()))
    val expectedValue = "foobar" |^| ("baz" |:| ("@" |^| (SE.ref |:| expectedCapturedV)))
    assert(rhs === expectedValue)
  }

  it should "captured vertex has same reference" in {
    val v = parseFirstRoot("A[foo](bar: baz) -> A").asInstanceOf[AstRule]
    val graph = v.toGraph(LexicalContext.empty)
    val rhs = graph.edges(SE.rhs).head.dst
    val lhs = graph.edges(SE.lhs).head.dst
    val captured = rhs.edges(SE.ref).head.dst
    assert(captured eq lhs)
  }
  def mkRefVertex(refTo: core.Vertex) : core.Vertex = {
    "@" |^| ("_ref" |:| refTo)
  }

  it should "concrete complex graph" in {
    val v = parseFirstRoot("hoge(fuga: A[foo](bar: baz), piyo: B) -> x(y: A, z: B)").asInstanceOf[AstRule]
    val actualGraph = v.toGraph(LexicalContext.empty)
    val capAsA = "foo" |^| ("bar" |:| ("baz" |^|()))
    val capAsB = "*" |^| ()
    val expectedLhs = "hoge" |^| ("fuga" |:| capAsA, "piyo" |:| capAsB)
    val expectedRhs = "x" |^| ("y" |:| mkRefVertex(capAsA), "z" |:| mkRefVertex(capAsB))
    val expectedGraph = expectedLhs |->| expectedRhs
    assert(expectedGraph === actualGraph)
    assert(actualGraph.path(":_lhs/hoge/foo").exact eq actualGraph.path(":_rhs/x:y/@/*").exact)
    assert(actualGraph.path("/->:_lhs/hoge:piyo/*").exact eq actualGraph.path("/->:_rhs/x:z/@/*").exact)
  }
}
