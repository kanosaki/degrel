package degrel.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import degrel.engine.Chassis

class SessionNode(baseIsland: ActorRef, manager: ActorRef) extends Actor with ActorLogging {
  val localNode = LocalNode(context.system)
  val driverFactory = ClusterDriverFactory(localNode)
  val repo = RemoteRepository(manager)
  val chassis = Chassis(repo, driverFactory)
  import messages._

  override def receive = {
    case Run(graph) => {
      val unpacked = localNode.exchanger.unpack(graph)
      if (unpacked.isCell) {
        val driver = driverFactory.create(chassis, unpacked.asCell)
        driver.stepUntilStop()
        val packed = localNode.exchanger.packAll(driver.header)
        sender() ! messages.Fin(packed)
      }
    }
    case SendGraph(target, graph) => {
    }
    case messages.Push(msg) => {
      val unpacked = localNode.exchanger.unpack(msg)
      if (unpacked.isCell) {
        val driver = driverFactory.create(chassis, unpacked.asCell)
        driver.stepUntilStop()
        val packed = localNode.exchanger.packAll(driver.header)
        sender() ! messages.Fin(packed)
      }

    }
  }
}

object SessionNode {
  def props(baseIsland: ActorRef, manager: ActorRef) = Props(new SessionNode(baseIsland, manager))
}

