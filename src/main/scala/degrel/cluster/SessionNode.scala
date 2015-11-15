package degrel.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import degrel.engine.Chassis

class SessionNode(baseIsland: ActorRef, manager: ActorRef, param: NodeInitializeParam) extends JournalingActor {

  val localNode = LocalNode(context.system)
  val driverFactory = ClusterDriverFactory(localNode)
  val repo = RemoteRepository(manager)
  val chassis = Chassis(repo, driverFactory)
  import messages._
  import context.dispatcher

  override def receive = {
    case QueryStatus() => {
      sender() ! NodeState()
    }
    case SendGraph(target, graph) => {
      localNode.lookupOwner(target) map {
        case Right(drv) => {
        }
        case Left(err) => {
          sender() ! "Cannto send data"
        }
      }
    }
    case LookupDriver(id) => {

    }
    case Run(msg) => {
      this.journal(Journal.Info("Push"))
      val unpacked = localNode.exchanger.unpack(msg)
      if (unpacked.isCell) {
        val driver = driverFactory.create(chassis, unpacked.asCell)
        driver.stepUntilStop()
        val packed = localNode.exchanger.packAll(driver.header)
        sender() ! messages.Fin(packed)
      }
    }
  }

  override def nodeID: NodeID = param.id

  override def journalCollector: ActorRef = manager
}

object SessionNode {
  def props(baseIsland: ActorRef, manager: ActorRef, param: NodeInitializeParam) = Props(new SessionNode(baseIsland, manager, param))
}

