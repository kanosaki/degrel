package degrel.core

import org.scalatest.FlatSpec
import degrel.engine.LocalDriver

import scala.concurrent.Await
import scala.concurrent.duration._

class IDTest extends FlatSpec {
  import scala.concurrent.ExecutionContext.Implicits.global

  "ID" should "compare by value" in {
    val i1 = GlobalID(1, 2, 3)
    val i2 = GlobalID(1, 2, 3)
    assert(i1 === i2)
  }

  it should "generated uniquely" in {
    assert(ID.nextLocalID() != ID.nextLocalID())
    assert(ID.nextLocalID() != ID.nextLocalID())
  }

  "NotAssignedID" should "automatically assigned" in {
    val v = Vertex("foobar", Seq(), Map())
    val d1 = LocalDriver()
    Await.result(d1.send(v), 5.seconds)
    assert(v.id.ownerID == d1.header.id.ownerID)
  }


}
