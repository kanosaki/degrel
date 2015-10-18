package degrel.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import degrel.control.Interpreter
import degrel.engine.Chassis

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ClusterInterpreter(override val chassis: Chassis) extends Interpreter(chassis) with Actor with ActorLogging {
  import degrel.cluster.messages._

  val system = ActorSystem("degrel")
  val cluster = Cluster(system)

  override def startProcess(): Long = {
    val island = system.actorOf(Props[Island])
    island ! Push(chassis.main.cell)
    Await.result(system.whenTerminated, Duration.Inf)
    0
  }

  override def receive: Receive = {
    case Fin(result) => {
      system.terminate()
    }
  }
}
