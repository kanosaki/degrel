package degrel.cluster

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}

import scala.collection.mutable

/**
  * Worker actor for cluster
  */
class Island extends Actor with ActorLogging {
  val cluster = Cluster(context.system)
  val id: Int = cluster.selfUniqueAddress.uid

  val islands = mutable.ListBuffer[ActorSelection]()
  val controllers = mutable.ListBuffer[ActorSelection]()


  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def register(member: Member): Unit = {
    if (member.hasRole("controller")) {
      log.warning(s"================ REGISTER $member ===============")
      val actors = context.actorSelection(RootActorPath(member.address) / "user" / "engine")
      actors ! messages.IslandRegistration
      controllers += actors
    }
    if (member.hasRole("island")) {
      val actors = context.actorSelection(RootActorPath(member.address) / "user" / "island")
      //actors ! messages.IslandRegistration
      islands += actors
    }
  }

  override def receive = {
    case MemberUp(member) => this.register(member)
    case state: CurrentClusterState => {
      // initialize message
      state.members.foreach(this.register)
    }
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
    case e: MemberEvent => log.info(s"MemberEvent: $e")
  }
}

object Island {
}