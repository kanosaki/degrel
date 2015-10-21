package degrel.cluster

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}


/**
 * One instance per JVM (= memory space)
 * Context class for Cluster
 */
class LocalNode(val name: String, val config: Config) {
  val info = NodeInfo.generate()
  val system = ActorSystem(name, config)
  val exchanger = new SpaceExchanger()(this)
}

object LocalNode {
  lazy val current = new LocalNode("degrel", ConfigFactory.load())
}
