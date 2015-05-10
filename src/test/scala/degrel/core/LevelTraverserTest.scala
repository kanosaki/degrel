package degrel.core

import org.scalatest.FlatSpec

class LevelTraverserTest extends FlatSpec {

  def mapLabels(src: Iterable[Iterable[Vertex]]): Seq[Seq[String]] = src.map(_.map(_.label.expr).toSeq).toSeq

  "LevelTraverser" should "traverse tree" in {
    val tree = degrel.parseVertex(
      """
        |a0(b0(c0(d0, d1), c1),
        |   b1(c2, c3),
        |   b2(c4))
      """.stripMargin)
    val params = Seq(
      0 -> Seq(Seq("a0")),
      1 -> Seq(Seq("a0"), Seq("b0", "b1", "b2")),
      2 -> Seq(Seq("a0"), Seq("b0", "b1", "b2"), Seq("c0", "c1", "c2", "c3", "c4")),
      3 -> Seq(Seq("a0"), Seq("b0", "b1", "b2"), Seq("c0", "c1", "c2", "c3", "c4"), Seq("d0", "d1"))
    )
    for ((depth, expected) <- params) {
      val actual = mapLabels(LevelTraverser(tree, depth))
      assert(actual === expected)
    }
  }

  it should "traverse DAG" in {
    val tree = degrel.parseVertex(
      """
        | a(b(c@C(d)), e(C))
      """.stripMargin)
    val params = Seq(
      0 -> Seq(Seq("a")),
      1 -> Seq(Seq("a"), Seq("b", "e")),
      2 -> Seq(Seq("a"), Seq("b", "e"), Seq("c")),
      3 -> Seq(Seq("a"), Seq("b", "e"), Seq("c"), Seq("d"))
    )
    for ((depth, expected) <- params) {
      val actual = mapLabels(LevelTraverser(tree, depth))
      assert(actual === expected)
    }
  }

  it should "traverse simple graph with cycle" in {
    val tree = degrel.parseVertex(
      """
        | a(b(d(e@E(f@F(E)), x(y, z))), c(F), F)
      """.stripMargin)
    val params = Seq(
      0 -> Seq(Seq("a")),
      1 -> Seq(Seq("a"), Seq("b", "c", "f")),
      2 -> Seq(Seq("a"), Seq("b", "c", "f"), Seq("d", "e")),
      3 -> Seq(Seq("a"), Seq("b", "c", "f"), Seq("d", "e"), Seq("x"))
    )
    for ((depth, expected) <- params) {
      val actual = mapLabels(LevelTraverser(tree, depth))
      assert(actual === expected)
    }
  }
}
