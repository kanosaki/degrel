package degrel.core

import org.scalatest.FlatSpec

class LabelTest extends FlatSpec {

  "A label" should "equals to its instances" in {
    val l1 = Label("foobar")
    val l2 = Label("foobar")
    val l3 = Label('foobar)
    val l4 = Label('piyopiyo)
    assert(l1 === l2)
    assert(l1 === l3)
    assert(l1 !== l4)
  }

  it should "equals to string instances" in {
    val l = Label("foobar")
    assert(l === "foobar")
    assert(l === 'foobar)
  }

  it should "denoted as meta if it starts with double underscore" in {
    assert(Label("__foobar__").isMeta)
    assert(!Label("_foobar").isMeta)
    assert(!Label("foobar").isMeta)
  }
}
