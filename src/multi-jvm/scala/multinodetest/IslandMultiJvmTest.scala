package multinodetest

import akka.actor.Props
import akka.cluster.Cluster
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory
import degrel.cluster.{Engine, Island}
import org.scalatest._

import scala.concurrent.duration._
import scala.language.postfixOps
import degrel.utils.TestUtils._


object IslandConfig extends MultiNodeConfig {
  val controller = role("controller")
  val island = role("island")

  def nodeList = Seq(controller, island)


  nodeList foreach { role =>
    nodeConfig(role) {
      ConfigFactory.parseString(
        s"""
          # Disable legacy metrics in akka-cluster.
          akka.cluster.metrics.enabled=off
          # Enable metrics extension in akka-cluster-metrics.
          akka.extensions=["akka.cluster.metrics.ClusterMetricsExtension"]
          # Sigar native library extract location during tests.
          akka.cluster.metrics.native-library-extract-folder=target/native/${role.name}
          akka.persistence.journal.plugin = "akka.persistence.journal.leveldb-shared"
          akka.persistence.journal.leveldb-shared.store {
            native = off
            dir = "target/test-shared-journal"
          }
          akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
          akka.persistence.snapshot-store.local.dir = "target/test-snapshots"
      """)
    }
  }

  commonConfig(ConfigFactory.parseString(
    """
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.remote.log-remote-lifecycle-events = off
    """).withFallback(ConfigFactory.load()))

  nodeConfig(controller)(
    ConfigFactory.parseString("akka.cluster.roles =[controller]"))

  nodeConfig(island)(
    ConfigFactory.parseString("akka.cluster.roles =[island]"))
}

abstract class IslandTestBase extends MultiNodeSpec(IslandConfig)
with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {

  import IslandConfig._
  import Engine.messages._

  "Island" must {
    "setup" in {

      runOn(controller) {
        Cluster(system) join node(controller).address
        system.actorOf(Props[Engine], name = "engine")
      }
      enterBarrier("engine-ready")

      runOn(island) {
        Cluster(system) join node(controller).address
        system.actorOf(Props[Island], name = "island")
      }
      enterBarrier("setup-complete")
    }

    "interpret a graph" in within(15.seconds) {
      runOn(controller) {
        val engine = system.actorSelection(s"akka://${system.name}/user/engine")
        val req = degrel.parseVertex("{fin a}").asCell
        val expected = degrel.parseVertex("a")
        engine ! Rewrite(req)
        expectMsgPF() {
          case Result(msg) => {
            log.info(s"*** ${req.pp} ===> ${msg.pp}")
            msg ===~ expected
          }
        }
      }
      enterBarrier("finish-return-empty")
    }
  }

  override protected def beforeAll(): Unit = multiNodeSpecBeforeAll()

  override protected def afterAll(): Unit = multiNodeSpecAfterAll()

  override def initialParticipants: Int = roles.size
}

class IslandMultiJvmController extends IslandTestBase

class IslandMultiJvmIsland extends IslandTestBase


