package degrel.tonberry

import org.scalatest.FlatSpec

import degrel.front
import degrel.core
import degrel.Query._
import degrel.utils.TestUtils._

class TonberryTest extends FlatSpec {
  val parser = front.DefaultTermParser

  def parse(expr: String) = {
    parser(expr).root.asInstanceOf[front.AstGraph].roots(0).toGraph(front.LexicalContext.empty)
  }

  def V(expr: String) = {
    core.Vertex(expr, Nil)
  }

  it should "select a child node thru edge" in {
    val v = parse("foo(bar: baz)")
    assert(v.nextE("bar").dst.exact ===~ V("baz"))
  }

  it should "select child nodes thru edge" in {
    val v = parse("foo(x: bar, x: baz)")
    assertElementSet(v.nextE("x").dst.toSet, Set(V("bar"), V("baz")))
  }

  it should "select a child node as neigbor" in {
    val v = parse("foo(bar: baz)")
    val byquery = v.nextV("baz").exact
    val bypath = v.path("baz").exact
    val expected = V("baz")
    assert(byquery ===~ expected)
    assert(bypath ===~ expected)
  }

  it should "select children by multi step thru edge" in {
    val v = parse("foo(bar: baz(hoge: fuga(piyo: x), y: z))")
    val queries = Seq(v.nextE("bar").dst.nextE("hoge").dst.nextE("piyo").dst.exact,
                      v.nextE("bar").nextE("hoge").nextE("piyo").dst.exact,
                      v.path(":bar/:hoge/:piyo/*").exact)
    val expected = V("x")
    for (q <- queries) {
      assert(expected ===~ q)
    }
  }

  it should "select a child multi step thru neighbor" in {
    val v = parse("foo(bar: baz(hoge: fuga(piyo: x), y: z))")
    val queries = Seq(v.nextV("baz").nextV("fuga").nextV().exact,
                      v.path("baz/fuga/*").exact)
    val expected = V("x")
    for (q <- queries) {
      assert(expected ===~ q)
    }
  }

  it should "select multi children thru edge" in {
    val v = parse("root(foo: bar1(baz: x), foo: bar2(baz: y), foo: bar3(baz: z))")
    val queries = Seq(v.nextE("foo").nextE("baz").dst,
                      v.path(":foo/:baz/*"))
    val expected = Set(V("x"), V("y"), V("z"))
    for (q <- queries) {
      assertElementSet(expected, q.toSet)
    }
  }

  it should "select multi children thru neighbor" in {
    val v = parse("root(foo1: bar(baz: x), foo2: bar(baz: x), foo3: bar(baz: x))")
    val queries = Seq(v.nextV("bar").nextV("x"),
                      v.nextV("bar").nextE("baz").dst,
                      v.nextV("bar").nextE("baz").nextV("x"),
                      v.path("bar/x") /* ,
                      v.find("x"),
                      v.find("baz/x") */)
    val expected = Set(V("x"), V("x"), V("x"))
    for (q <- queries) {
      assertElementSet(expected, q.toSet)
    }
  }
}

