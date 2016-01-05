package degrel.cluster

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.typesafe.config.ConfigFactory
import degrel.core.{Cell, Vertex}
import degrel.engine.LocalDriver
import org.scalatest.FlatSpec
import degrel.utils.TestUtils._

import scala.concurrent.ExecutionContext

class LocalNodeTest extends FlatSpec {
  implicit val context = ExecutionContext.Implicits.global

  def channelThrough(v: Vertex): Vertex = {
    val node = LocalNode()
    val exchanger = node.exchanger
    val dgraph = exchanger.packAll(v)
    val out = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(out)
    oos.writeObject(dgraph)
    val input = new ByteArrayInputStream(out.toByteArray)
    val ois = new ObjectInputStream(input)
    val data = ois.readObject().asInstanceOf[DGraph]
    exchanger.unpack(data)
  }

  def checkSameBehavior(cell: Cell) = {
    val channeled = channelThrough(cell).asCell
    val driver1 = LocalDriver(cell)
    driver1.stepUntilStop()
    val driver2 = LocalDriver(channeled)
    driver2.stepUntilStop()
    assert(driver1.header ===~ driver2.header)
  }

  "Exchanger" should "packAll vertices" in {
    val expected = degrel.parseVertex("foo(bar, baz, hoge: fuga(foo, bar))")
    assert(channelThrough(expected) ===~ expected)
  }

  it should "packAll cyclic vertices" in {
    val expected = degrel.parseVertex("foo@X(bar, baz, hoge: fuga(foo, bar, X))")
    assert(channelThrough(expected) ===~ expected)
  }

  it should "packAll cell" in {
    val expected = degrel.parseVertex("{foo; bar; hoge(@X) -> fuga(X)}")
    assert(channelThrough(expected) ===~ expected)
  }

  it should "keep same behavior thru network with a simple cell" in {
    val data = degrel.parseVertex(
      """
        |{
        | a
        | a -> b
        | b -> c
        |}
      """.stripMargin).asCell
    checkSameBehavior(data)
  }

  it should "keep same behavior thru network with a cell which has binding" in {
    val data = degrel.parseVertex(
      """
        |{
        | foo(bar)
        | foo(@X) -> hoge(fuga: X)
        | hoge(fuga: @X) -> piyo(X)
        |}
      """.stripMargin).asCell
    checkSameBehavior(data)
  }

  it should "keep same behavior thru network with a cell which has subcell" in {
    val data = degrel.parseVertex(
      """{
        | foo(hoge)
        | foo(@X) -> {
        |   fin foobar(X)
        | }
        |}
      """.stripMargin).asCell
    checkSameBehavior(data)
  }

}
