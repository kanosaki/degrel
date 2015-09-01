package degrel.core

import org.scalatest.FlatSpec

import scala.collection.mutable
import degrel.utils.TestUtils._

class FingerprintTest extends FlatSpec {
  def parse(s: String): Vertex = degrel.parseVertex(s)

  def reprFingerprintDetail(v1: Vertex, v2: Vertex)(implicit fp: Fingerprint): String = {
    val ppTable = mutable.HashMap[Vertex, String]()
    (Traverser(v1) ++ Traverser(v2)).foreach { v =>
      ppTable += v -> v.pp
    }
    val ppColLen = ppTable.values.map(_.length).max
    val ppFormat = s"%${ppColLen + 2}s"
    val fp = Fingerprint.default
    val sb = new StringBuilder()
    sb ++= s"\n----- v1 : ${v1.pp}\n"
    Traverser(v1).foreach { v =>
      sb ++= String.format(ppFormat, ppTable(v))
      sb ++= s": ${fp.formatBits(fp.get(v))}\n"
    }

    sb ++= s"\n----- v2 : ${v2.pp}\n"
    Traverser(v2).foreach { v =>
      sb ++= String.format(ppFormat, ppTable(v))
      sb ++= s": ${fp.formatBits(fp.get(v))}\n"
    }
    sb.toString()
  }

  def fpAssert(v1: Vertex, v2: Vertex)(implicit fp: Fingerprint) = {
    assert(v1 ===~ v2)
    assert(fp.get(v1) === fp.get(v2), reprFingerprintDetail(v1, v2))
  }

  def fpAssertMatch(v1: Vertex, v2: Vertex)(implicit fp: Fingerprint) = {
    //assert(v2.matches(v1).success, s"${v1.pp} not matches ${v2.pp}")
    val v1Fp = fp.get(v1)
    val v2Fp = fp.get(v2)
    assert((v1Fp & v2Fp) === v1Fp, reprFingerprintDetail(v1, v2))
  }

  Seq(//new HeadTailFingerprint(16),
      new DepthFingerprint(20, 3),
      new PathFingerprint(20, 3)
  ).foreach { implicit fp =>
    fp.getClass.getName should "same fingerprint for same vertex" in {
      val data = Seq(
        "a" -> "a",
        "_" -> "_",
        "foo(bar: baz, hoge: fuga)" -> "foo(bar: baz, hoge: fuga)",
        "foo(bar: baz, hoge: fuga)" -> "foo(hoge: fuga, bar: baz)",
        "foo(a: hoge, b: fuga(x: piyo, y: foobar), c: baz)" ->
          "foo(b: fuga(y: foobar, x: piyo), c: baz, a: hoge)",
        "foo(x: bar(baz), x: hoge(fuga))" ->
          "foo(x: hoge(fuga), x: bar(baz))",
        "foo(x: bar(x: baz, x: a), x: hoge(x: fuga, x: b))" ->
          "foo(x: hoge(x: b, x: fuga), x: bar(x: a, x: baz))",
        "x(y: y@Y(z: Z), z: z@Z(y: Y))" -> "x(z: z@Z(y: Y), y: y@Y(z: Z))",
        "x(y: y@Y(z: Z), z: z@Z(y: Y))" -> "x(y: y@Y(z: Z), z: z@Z(y: Y))",
        "x(y: y@Y(z: Z, a: b), z: z@Z(y: Y, c: d))" -> "x(z: z@Z(y: Y, c: d), y: y@Y(z: Z, a: b))",
        "x(a: y@Y(a: Z, a: b), a: z@Z(a: Y, a: d))" -> "x(a: z@Z(a: Y, a: d), a: y@Y(a: Z, a: b))"
      )
      data.foreach {
        case (leftStr, rightStr) =>
          val left = parse(leftStr)
          val right = parse(rightStr)
//          fp.printlnBits(fp.get(left))
          fpAssert(left, right)
      }
    }

    it should "satisfy match property" in {
      val data = Seq(
        "a" -> "a",
        "_" -> "a",
        "foo(bar: _, hoge: _)" -> "foo(bar: baz, hoge: fuga)",
        "foo(bar: baz)" -> "foo(hoge: fuga, bar: baz)",
        "foo(a: _, b: fuga(x: _, y: foobar), c: baz)" ->
          "foo(b: fuga(y: foobar, x: piyo), c: baz, a: hoge)",
        "foo(x: _(baz), x: hoge(_))" ->
          "foo(x: hoge(fuga), x: bar(baz))",
        "foo(x: bar(x: _, x: a), x: hoge(x: fuga, x: _))" ->
          "foo(x: hoge(x: b, x: fuga), x: bar(x: a, x: baz))",
        "x(y: y@Y(z: Z), z: z@Z(y: Y))" -> "x(z: z@Z(y: Y), y: y@Y(z: Z))",
        "x(y: y@Y(z: Z), z: z@Z(y: Y))" -> "x(y: y@Y(z: Z), z: z@Z(y: Y))",
        "x(y: y@Y(z: Z, a: _), z: z@Z(y: Y, c: _))" -> "x(z: z@Z(y: Y, c: d), y: y@Y(z: Z, a: b))",
        "x(a: y@Y(a: Z, a: _), a: z@Z(a: Y, a: _))" -> "x(a: z@Z(a: Y, a: d), a: y@Y(a: Z, a: b))"
      )
      data.foreach {
        case (leftStr, rightStr) =>
          val left = parse(leftStr)
          val right = parse(rightStr)
//          println("-------------------------")
//          fp.printlnBits(fp.get(left))
//          fp.printlnBits(fp.get(right))
          fpAssertMatch(left, right)
      }
    }
  }

}
