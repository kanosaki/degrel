package degrel.cluster

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import akka.util.Timeout
import degrel.cluster.messages.{TellLobby, WorkerRegistration}

import scala.collection.mutable

trait MemberBase extends ActorBase {

  import MemberBase._
  import context.dispatcher

  val cluster = Cluster(context.system)
  val controllers = mutable.ListBuffer[ActorRef]()
  val workers = mutable.ListBuffer[ActorRef]()
  val lobbies = mutable.ListBuffer[ActorRef]()


  def joinLobby(addr: Address) = {
    //Cluster.get(context.system).joinSeedNodes(immutable.Seq(addr))
    cluster.join(addr)
    actorOfRole(addr, Roles.Lobby) ! WorkerRegistration(self)
    this.onLobbyJoined(addr)
    log.info(s"*************** Joined to Lobby: $addr ${cluster.state}")
  }

  protected def onLobbyJoined(addr: Address) = {}

  def register(member: Member)(implicit timeout: Timeout = Timeouts.short): Unit = {
    log.info(s"*************** Accept: ${member.address} roles: ${member.roles} members: ${Cluster.get(context.system).state.members.size}")
    if (member.hasRole(Roles.Controller.name)) {
      context.actorSelection(actorPath(member.address, Roles.Controller)).resolveOne() map { ref =>
        controllers += ref
      }
    }
    if (member.hasRole(Roles.Worker.name)) {
      context.actorSelection(actorPath(member.address, Roles.Worker)).resolveOne() map { ref =>
        workers += ref
      }
    }
    if (member.hasRole(Roles.Lobby.name)) {
      context.actorSelection(actorPath(member.address, Roles.Lobby)).resolveOne() map { ref =>
        lobbies += ref
      }
    }
  }

  protected def actorOfRole(addr: Address, role: MemberRole) = {
    context.actorSelection(actorPath(addr, role))
  }

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    println(s"!!!!!!!!!!! Stopped $self")
    cluster.unsubscribe(self)
  }

  def receiveMemberMsg: Receive = {
    case TellLobby(addr) => this.joinLobby(addr)
    case MemberUp(member) => this.register(member)
    case state: CurrentClusterState => {
      // initialize message
      state.members.foreach(this.register)
    }
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
    case e: MemberEvent => log.info(s"******************** MemberEvent: $e")
  }

  def receiveUnhandled: Receive = {
    case msg => {
      log.warning(s"Unknown message: $msg")
    }
  }

  def receiveMsg: Receive

  override def receive: Receive = receiveMsg.orElse(receiveMemberMsg).orElse(receiveUnhandled)
}

object MemberBase {
  def actorPath(addr: Address, role: MemberRole) = {
    RootActorPath(addr) / "user" / role.name
  }

}
