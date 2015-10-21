package degrel.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.cluster.Cluster
import degrel.control.Interpreter
import degrel.engine.Chassis

class ClusterInterpreter(override val chassis: Chassis) extends Interpreter(chassis) with Actor with ActorLogging {
  import degrel.cluster.messages._

  val system = ActorSystem("degrel")
  val cluster = Cluster(system)

  override def startProcess(): Long = {
    0
  }

  override def receive: Receive = {
    case Fin(result) => {
      system.terminate()
    }
  }
}
