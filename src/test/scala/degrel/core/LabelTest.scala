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
}
