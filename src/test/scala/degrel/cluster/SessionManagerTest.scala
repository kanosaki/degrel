package degrel.cluster

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import degrel.cluster.journal.Journal.CellSpawn
import degrel.cluster.journal.JournalPayload
import degrel.cluster.messages._
import degrel.utils.TestUtils._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class SessionManagerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("SessionManagerTest", ConfigFactory.load("degrel").withFallback(ConfigFactory.load())))

  "A empty SessionManager" must {
    "returns status (also tests ClusterTestUtils.newSession)" in {
      val session = ClusterTestUtils.newSession()
      session ! QueryStatus()
      expectMsgPF() {
        case _: SessionState =>
      }
    }
  }

  def runScript(before: String, after: String, nodeNum: Int): Seq[JournalPayload] = {
    val session = ClusterTestUtils.newSession(nodeNum)
    val code = degrel.parseVertex(before)
    val expected = degrel.parseVertex(after)
    val node = LocalNode(system)
    val dCode = node.exchanger.packAll(code)
    session ! StartInterpret(dCode, self)
    val packed = expectMsgPF(7.seconds) {
      case Fin(gr) => gr
    }
    val unpacked = node.exchanger.unpack(packed)
    assert(expected ===~ unpacked)
    session ! FetchJournal(false)
    expectMsgPF(3.seconds) {
      case Right(js) => js.asInstanceOf[Vector[JournalPayload]]
    }
  }


  "SessionManager" must {
    "Interprets simple script" in {
      val before = "{fin a}"
      val after = "a"
      this.runScript(before, after, 1)
    }

    "Interprets script (no chain spawn) with journal assertion" in {
      val before =
        """{
          | a
          | a
          | a
          | a
          | a
          | a -> {
          |   fin b
          | }
          |}
        """.stripMargin
      val after = "{b; b; b; b; b;  a -> {fin b}}"
      val journals = this.runScript(before, after, 2)
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      assert(spawns(0).spawnAt !== spawns(1).spawnAt) // manager spawn != first spawn
    }

    "Interprets script (multi non-chain spawns) with journal assertion" in {
      val before =
        """{
          | foo(a)
          | a -> c
          | c -> {
          |   fin b
          | }
          |}
        """.stripMargin
      val after = "{foo(b); a -> c; c -> {fin b}}"
      val journals = runScript(before, after, 2)
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      assert(spawns(0).spawnAt !== spawns(1).spawnAt) // manager spawn != first spawn
    }

    "Interprets script (multi chain spawns) with journal assertion" in {
      val before =
        """{
          | fin a
          | a -> {
          |   fin b
          | }
          | b -> {
          |   fin c
          | }
          |}
        """.stripMargin
      val after = "c"
      val journals = runScript(before, after, 2)
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      assert(spawns(0).spawnAt !== spawns(1).spawnAt) // manager spawn != first spawn
      assert(spawns(1).spawnAt !== spawns(2).spawnAt) // first spawn != child spawn
    }

    "Interprets script (single spawn, binding) with journal assertion" in {
      val before =
        """{
          | fin a(hoge)
          | a(@X) -> {
          |   fin b(X)
          | }
          |}
        """.stripMargin
      val after = "b(hoge)"
      val journals = runScript(before, after, 2)
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      assert(spawns(0).spawnAt !== spawns(1).spawnAt) // manager spawn != first spawn
    }

    "Interprets script (multi spawns, binding) with journal assertion" in {
      val before =
        """{
          | fin a(hoge)
          | a(@X) -> {
          |   fin b(X)
          | }
          | b(@X) -> {
          |   fin c(X)
          | }
          |}
        """.stripMargin
      val after = "c(hoge)"
      val journals = runScript(before, after, 2)
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      assert(spawns(0).spawnAt !== spawns(1).spawnAt) // manager spawn != first spawn
      assert(spawns(1).spawnAt !== spawns(2).spawnAt) // first spawn != child spawn
    }

    "Simple send message" in {
      val before =
        """{
          |  hoge
          |  {foo} ! bar
          |}
        """.stripMargin
      val after = "{hoge; {foo; bar}}"
      val journals = runScript(before, after, 2)
      val spawns = journals.map(_.item).collect {
        case cs: CellSpawn => cs
      }
      assert(spawns(0).spawnAt !== spawns(1).spawnAt) // manager spawn != first spawn
    }
  }
}
