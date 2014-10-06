package degrel.front

import org.scalatest.FlatSpec
import degrel.utils.TestUtils._

class AstExprTest extends FlatSpec {
  val parseDot = ParserUtils.parseDot _

  def vertex(label: String, edges: Seq[AstEdge] = Seq()) =
    AstVertex(AstName(None, Some(AstLabel(label))), None, edges)

  it should "Build expr ast" in {
    implicit val parserContext = ParserContext.default
    val firstTerm = vertex("a")
    val following = Seq(
      AstBinOp("+") -> vertex("b"),
      AstBinOp("->") -> vertex("c")
    )
    // a + b -> c
    val ast = AstExpr(firstTerm, following)
    val graph = ast.toGraph(LexicalContext.empty)
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
