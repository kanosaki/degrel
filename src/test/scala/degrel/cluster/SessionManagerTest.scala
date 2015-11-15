package degrel.cluster

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import degrel.cluster.messages._
import degrel.utils.TestUtils._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SessionManagerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("SessionManagerTest"))

  "A empty SessionManager" must {
    "returns status (also tests ClusterTestUtils.newSession)" in {
      val session = ClusterTestUtils.newSession
      session ! QueryStatus()
      expectMsgPF() {
        case _: SessionState =>
      }
    }
  }

  "SessionManager" must {
    "Interprets simple script" in {
      val session = ClusterTestUtils.newSession
      val code = degrel.parseVertex("{fin a}")
      val expected = degrel.parseVertex("a")
      val node = LocalNode(system)
      val dCode = node.exchanger.packAll(code)
      session ! StartInterpret(dCode, self)
      val packed = expectMsgPF() {
        case Fin(gr) => gr
      }
      val unpacked = node.exchanger.unpack(packed)
      assert(expected ===~ unpacked)
    }

    "Interprets simple script with journal assertion" in {
      val session = ClusterTestUtils.newSession
      val code = degrel.parseVertex(
        """{
          | fin a
          | a -> {
          |   fin b
          | }
          |}
        """.stripMargin)
      val expected = degrel.parseVertex("b")
      val node = LocalNode(system)
      val dCode = node.exchanger.packAll(code)
      session ! StartInterpret(dCode, self)
      val packed = expectMsgPF() {
        case Fin(gr) => gr
      }
      val unpacked = node.exchanger.unpack(packed)
      assert(expected ===~ unpacked)

      session ! FetchJournal(false)
      val journals = expectMsgPF() {
        case Right(js) => js.asInstanceOf[Vector[JournalPayload]]
      }
      println(journals) // assert journal here
    }
  }
}
