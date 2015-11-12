package multinodetest

import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory
import degrel.cluster.Timeouts
import degrel.control.cluster._
import degrel.utils.TestUtils._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

object InterpretConfig extends MultiNodeConfig {
  val controller = role("controller")
  val worker1 = role("worker1")
  val worker2 = role("worker2")
  val lobby = role("lobby")

  def nodeList = Seq(controller, lobby, worker1, worker2)

  val roleMapping = Map(
    "controller" -> "controller",
    "worker1" -> "worker",
    "worker2" -> "worker",
    "lobby" -> "lobby")


  nodeList foreach { role =>
    nodeConfig(role) {
      ConfigFactory.parseString(
        s"""
          seed-nodes = [
            "akka.tcp://ClusterSystem@127.0.0.1:2551",
            "akka.tcp://ClusterSystem@127.0.0.1:2552"]
          multinode.max-nodes = 10
          akka.cluster.roles = [${roleMapping(role.name)}]
          # Disable legacy metrics in akka-cluster.
          akka.cluster.metrics.enabled=off
          # Enable metrics extension in akka-cluster-metrics.
          akka.extensions=["akka.cluster.metrics.ClusterMetricsExtension"]
          # Sigar native library extract location during tests.
          akka.cluster.metrics.native-library-extract-folder=target/native/${role.name}
          #akka.persistence.journal.plugin = "akka.persistence.journal.leveldb-shared"
          #akka.persistence.journal.leveldb-shared.store {
          #  native = off
          #  dir = "target/test-shared-journal"
          #}
          #akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
          #akka.persistence.snapshot-store.local.dir = "target/test-snapshots"
        """
      )
    }
  }

  commonConfig(ConfigFactory.parseString(
    """
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.remote.log-remote-lifecycle-events = off
    """).withFallback(ConfigFactory.load()))

}

abstract class InterpretTestBase extends MultiNodeSpec(InterpretConfig)
with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {

  override def initialParticipants: Int = roles.size

  import InterpretConfig._

  "Interpret" must {
    "interpret data" in {
      var lobbyFacade: LobbyFacade = null
      var workerFacade: WorkerFacade = null
      runOn(lobby) {
        lobbyFacade = LobbyFacade(system)
        lobbyFacade.start()
      }

      runOn(worker1, worker2) {
        workerFacade = WorkerFacade(system, node(lobby).address)
        workerFacade.start()
      }

      runOn(controller) {
        val code = degrel.parseVertex("{fin foo}")
        val expected = degrel.parseVertex("foo")
        val controllerFacade = ControllerFacade(system, node(lobby).address)
        controllerFacade.waitForReady()
        val fut = controllerFacade.interpret(code.asCell)
        val res = Await.result(fut, Timeouts.short.duration)
        assert(res ===~ expected)
      }
      enterBarrier("finish")
    }
  }

  override protected def beforeAll(): Unit = multiNodeSpecBeforeAll()

  override protected def afterAll(): Unit = multiNodeSpecAfterAll()

}

class InterpretMultiJvmController extends InterpretTestBase

class InterpretMultiJvmWorker1 extends InterpretTestBase

class InterpretMultiJvmWorker2 extends InterpretTestBase

class InterpretMultiJvmLobby extends InterpretTestBase
