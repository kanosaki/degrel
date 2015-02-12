package degrel.dgspec

import org.scalatest.FlatSpec
import org.scalatest.exceptions.TestFailedException

class SpecFileTest extends FlatSpec {
  def toSpec(in: String) = {
    val mapper = SpecFile.defaultMapper
    implicit val factory = new SpecFactory()
    val tree = mapper.readTree(in)
    SpecFile.decode(tree)
  }

  "Minimal spec" should "pass" in {
    val spec = toSpec(
      """
        |description: Hogehoge
        |version: 1
      """.stripMargin)
    assert(spec.description === "Hogehoge")
    assert(spec.evaluate(SpecContext.empty()) === NextPiece.Continue)
  }

  "Simple set and assert" should "pass in same vertices" in {
    val spec = toSpec(
      """
        |description: Hogehoge
        |version: 1
        |seq_spec:
        |  - init: >
        |        a(b: c) -> e
        |  - assert: >
        |        a(b: c) -> e
      """.stripMargin)
    assert(spec.evaluate(SpecContext.empty()) === NextPiece.Continue)
  }

  it should "pass in same cells" in {
    val spec = toSpec(
      """
        |description: Hogehoge
        |version: 1
        |seq_spec:
        |  - init: >
        |      {
        |        a
        |        a(b: c)
        |        d -> e
        |      }
        |  - assert: >
        |      {
        |        a(b: c)
        |        a
        |        d -> e
        |      }
      """.stripMargin)
    assert(spec.evaluate(SpecContext.empty()) === NextPiece.Continue)
  }

  it should "fail in difference vertices" in {
    val spec = toSpec(
      """
        |description: Hogehoge
        |version: 1
        |seq_spec:
        |  - init: >
        |        a(b: c)
        |  - assert: >
        |        a(b: d)
      """.stripMargin)
    intercept[TestFailedException] {
      spec.evaluate(SpecContext.empty())
    }
  }

  it should "fail in difference cells" in {
    val spec = toSpec(
      """
        |description: Hogehoge
        |version: 1
        |seq_spec:
        |  - init: >
        |      {
        |        a
        |        a(b: c)
        |        d -> e
        |      }
        |  - assert: >
        |      {
        |        a(b: c)
        |        a
        |        d -> f
        |      }
      """.stripMargin)
      intercept[TestFailedException] {
        spec.evaluate(SpecContext.empty())
      }
  }

  "Set, rewrite and assert" should "pass in empty cell" in {
    val spec = toSpec(
      """
        |description: Hogehoge
        |version: 1
        |seq_spec:
        |  - init: >
        |      {
        |      }
        |  - rewrite
        |  - assert: >
        |      {
        |      }
      """.stripMargin)
    assert(spec.evaluate(SpecContext.empty()) === NextPiece.Continue)
  }

  it should "pass single rewriting cell" in {
    val spec = toSpec(
      """
        |description: Hogehoge
        |version: 1
        |seq_spec:
        |  - init: >
        |      {
        |       a
        |       a -> b
        |      }
        |  - rewrite
        |  - assert: >
        |      {
        |       b
        |       a -> b
        |      }
      """.stripMargin)
    assert(spec.evaluate(SpecContext.empty()) === NextPiece.Continue)
  }
}
