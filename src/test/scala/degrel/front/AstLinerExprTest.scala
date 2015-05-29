package degrel.front

import degrel.graphbuilder
import org.scalatest.FlatSpec
import degrel.utils.TestUtils._

class AstLinerExprTest extends FlatSpec {
  val parseDot = ParserUtils.parseDot _

  def vertex(label: String, edges: Seq[AstEdge] = Seq()) =
    AstFunctor(AstName(Some(AstLabel(label)), None), None, AstEdges(edges, Seq()))

  it should "Build expr ast" in {
    implicit val parserContext = ParserContext.default
    val firstTerm = vertex("a")
    val following = Seq(
      AstBinOp("+") -> vertex("b"),
      AstBinOp("->") -> vertex("c")
    )
    // a + b -> c
    val ast = AstLinerExpr(firstTerm, following).toTree
    val graph = graphbuilder.build(ast)
    val expected = parseDot(
      """@ '->' {
         |   -> c : __rhs__
         |   -> '+' : __lhs__
         |   '+' -> a : __lhs__
         |   '+' -> b : __rhs__
         | }
      """.stripMargin)
    assert(graph ===~ expected)
  }
}
