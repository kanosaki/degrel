package degrel.cluster

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import degrel.cluster.Journal.CellSpawn
import degrel.cluster.messages._
import degrel.utils.TestUtils._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

import scala.concurrent.duration.Duration

class SessionManagerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("SessionManagerTest"))

  "A empty SessionManager" must {
    "returns status (also tests ClusterTestUtils.newSession)" in {
      val session = ClusterTestUtils.newSession()
      session ! QueryStatus()
      expectMsgPF() {
        case _: SessionState =>
      }
    }
  }

  "SessionManager" must {
    "Interprets simple script" in {
      val session = ClusterTestUtils.newSession()
      val code = degrel.parseVertex("{fin a}")
      val expected = degrel.parseVertex("a")
      val node = LocalNode(system)
      val dCode = node.exchanger.packAll(code)
      session ! StartInterpret(dCode, self)
      val packed = expectMsgPF(5.seconds) {
        case Fin(gr) => gr
      }
      val unpacked = node.exchanger.unpack(packed)
      assert(expected ===~ unpacked)
    }

    "Interprets script (a spawn) with journal assertion" in {
      val session = ClusterTestUtils.newSession(2)
      val code = degrel.parseVertex(
        """{
          | a
          | a -> {
          |   fin b
          | }
          |}
        """.stripMargin)
      val expected = degrel.parseVertex("{b; a -> {fin b}}")
      val node = LocalNode(system)
      val dCode = node.exchanger.packAll(code)
      session ! StartInterpret(dCode, self)
      val packed = expectMsgPF(7.seconds) {
        case Fin(gr) => gr
      }
      val unpacked = node.exchanger.unpack(packed)
      assert(expected ===~ unpacked)

      session ! FetchJournal(false)
      val journals = expectMsgPF() {
        case Right(js) => js.asInstanceOf[Vector[JournalPayload]]
      }
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      println(spawns)
      assert(spawns(0).spawnAt != spawns(1).spawnAt) // manager spawn != first spawn
      assert(spawns(1).spawnAt != spawns(2).spawnAt) // first spawn != child spawn
    }

    "Interprets script (multi spawns) with journal assertion" in {
      val session = ClusterTestUtils.newSession(2)
      val code = degrel.parseVertex(
        """{
          | a
          | a -> {
          |   fin b
          | }
          | b -> {
          |   fin c
          | }
          |}
        """.stripMargin)
      val expected = degrel.parseVertex("{c; a -> {fin b}; b -> {fin c}}")
      val node = LocalNode(system)
      val dCode = node.exchanger.packAll(code)
      session ! StartInterpret(dCode, self)
      val packed = expectMsgPF(7.seconds) {
        case Fin(gr) => gr
      }
      val unpacked = node.exchanger.unpack(packed)
      assert(expected ===~ unpacked)

      session ! FetchJournal(false)
      val journals = expectMsgPF() {
        case Right(js) => js.asInstanceOf[Vector[JournalPayload]]
      }
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      assert(spawns(0).spawnAt != spawns(1).spawnAt) // manager spawn != first spawn
      assert(spawns(1).spawnAt != spawns(2).spawnAt) // first spawn != child spawn
    }

    "Interprets script (multi spawns, binding) with journal assertion" in {
      val session = ClusterTestUtils.newSession(2)
      val code = degrel.parseVertex(
        """{
          | a(hoge)
          | a(@X) -> {
          |   fin b(X)
          | }
          | b(@X) -> {
          |   fin c(X)
          | }
          |}
        """.stripMargin)
      val expected = degrel.parseVertex("{c(hoge); a -> {fin b}; b -> {fin c}}")
      val node = LocalNode(system)
      val dCode = node.exchanger.packAll(code)
      session ! StartInterpret(dCode, self)
      val packed = expectMsgPF(7.seconds) {
        case Fin(gr) => gr
      }
      val unpacked = node.exchanger.unpack(packed)
      assert(expected ===~ unpacked)

      session ! FetchJournal(false)
      val journals = expectMsgPF() {
        case Right(js) => js.asInstanceOf[Vector[JournalPayload]]
      }
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      assert(spawns(0).spawnAt != spawns(1).spawnAt) // manager spawn != first spawn
      assert(spawns(1).spawnAt != spawns(2).spawnAt) // first spawn != child spawn
    }
  }
}
