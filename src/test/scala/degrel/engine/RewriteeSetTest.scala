package degrel.engine

import degrel.engine.rewriting.Rewriter
import org.scalatest.FlatSpec

class RewriteeSetTest extends FlatSpec {
  "Plain rewritee" should "return candidates without any shortage, simple atoms" in {
    val cell = degrel.parseVertex(
      """{
        |  a
        |  b
        |  c
        |  a
        |  a -> b
        |}
      """.stripMargin).asCell
    val driver = LocalDriver(cell)
    driver.rewritee = new PlainRewriteeSet(driver)
    val r1 = Rewriter(driver.cell.rules.head)
    val candidates = driver.rewritee.targetsFor(r1)
    assert(candidates.size == 4)
  }

  it should "return candidates without any shortage, simple functor" in {
    val cell = degrel.parseVertex(
      """{
        |  foo(a)
        |  bar(b)
        |  baz(c)
        |  a -> b
        |}
      """.stripMargin).asCell
    val driver = LocalDriver(cell)
    driver.rewritee = new PlainRewriteeSet(driver)
    val r1 = Rewriter(driver.cell.rules.head)
    val candidates = driver.rewritee.targetsFor(r1)
    assert(candidates.size == 6)
    val foo = driver.atomTargets.find(_.target.label == "foo").get
    val bar = driver.atomTargets.find(_.target.label == "bar").get
    val baz = driver.atomTargets.find(_.target.label == "baz").get
    driver.writeVertex(baz, degrel.parseVertex("piyo(a)"))
    assert(driver.rewritee.targetsFor(r1).size === 6)
    driver.writeVertex(baz, degrel.parseVertex("piyo(x)"))
    assert(driver.rewritee.targetsFor(r1).size === 6)
    driver.writeVertex(foo, degrel.parseVertex("hoge(x)"))
    assert(driver.rewritee.targetsFor(r1).size === 6)
    driver.writeVertex(bar, degrel.parseVertex("fuga(a, b, c)"))
    assert(driver.rewritee.targetsFor(r1).size === 8)
  }

  "RootHashed rewritee" should "return candidates without any shortage, simple atoms" in {
    val cell = degrel.parseVertex(
      """{
        |  a
        |  b
        |  c
        |  a
        |  a -> b
        |}
      """.stripMargin).asCell
    val driver = LocalDriver(cell)
    driver.rewritee = new RootTableRewriteeSet(driver)
    val r1 = Rewriter(driver.cell.rules.head)
    val candidates = driver.rewritee.targetsFor(r1)
    assert(candidates.size == 2)
  }

  it should "return candidates without any shortage, simple functors" in {
    val cell = degrel.parseVertex(
      """{
        |  foo(a)
        |  bar(b)
        |  baz(c)
        |  a -> b
        |}
      """.stripMargin).asCell
    val driver = LocalDriver(cell)
    driver.rewritee = new RootTableRewriteeSet(driver)
    val r1 = Rewriter(driver.cell.rules.head)
    assert(driver.rewritee.targetsFor(r1).size == 2)
    val foo = driver.atomTargets.find(_.target.label == "foo").get
    val bar = driver.atomTargets.find(_.target.label == "bar").get
    val baz = driver.atomTargets.find(_.target.label == "baz").get
    driver.writeVertex(baz, degrel.parseVertex("piyo(a)"))
    assert(driver.rewritee.targetsFor(r1).size === 4)
    driver.writeVertex(baz, degrel.parseVertex("piyo(x)"))
    assert(driver.rewritee.targetsFor(r1).size === 2)
    driver.writeVertex(foo, degrel.parseVertex("hoge(x)"))
    assert(driver.rewritee.targetsFor(r1).isEmpty)
    driver.writeVertex(bar, degrel.parseVertex("fuga(a, b, c)"))
    assert(driver.rewritee.targetsFor(r1).size === 4)
  }
}
