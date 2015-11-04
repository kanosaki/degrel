package degrel.core

import org.scalatest.FlatSpec
import degrel.engine.LocalDriver

class IDTest extends FlatSpec {

  def own(target: Vertex, owner: Vertex) = transformer.own(target, owner)

  "ID" should "compare by value" in {
    val i1 = GlobalID(1, 2, 3)
    val i2 = GlobalID(1, 2, 3)
    assert(i1 === i2)
  }

  it should "generated uniquely" in {
    assert(ID.nextLocalVertexID() != ID.nextLocalVertexID())
    assert(ID.nextLocalCellID() != ID.nextLocalCellID())
  }

  "NotAssignedID" should "automatically assigned" in {
    val v = Vertex("foobar", Seq(), Map())
    val d1 = LocalDriver()
    d1.addRoot(v)
    assert(v.id.ownerID == d1.header.id.ownerID)
  }


}
