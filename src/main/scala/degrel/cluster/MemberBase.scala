package degrel.cluster

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import akka.util.Timeout
import degrel.cluster.messages.{MemberRegistration, TellLobby}

import scala.collection.mutable
import scala.concurrent.Promise
import scala.util.Success

trait MemberBase extends ActorBase {

  import MemberBase._
  import context.dispatcher

  val cluster = Cluster(context.system)
  val controllers = mutable.HashSet[ActorRef]()
  val workers = mutable.HashSet[ActorRef]()
  val lobbies = mutable.HashSet[ActorRef]()
  val addressMapping = new mutable.HashMap[Address, mutable.Set[ActorRef]]() with mutable.MultiMap[Address, ActorRef]

  protected val initialLobby: Promise[ActorRef] = Promise()

  def role: MemberRole

  def joinLobby(lobbyRef: ActorRef) = {
    lobbyRef ! MemberRegistration(this.role, self)
    cluster.join(lobbyRef.path.address)
    log.info(s"*************** Joining.... : $lobbyRef ${cluster.state}")
  }

  def joinLobby(addr: Address) = {
    //Cluster.get(context.system).joinSeedNodes(immutable.Seq(addr))
    cluster.join(addr)
    log.info(s"*************** Joining.... : $addr ${cluster.state}")
  }

  protected def onLobbyJoined(addr: Address) = {
    log.info(s"*************** Joined to Lobby: $addr ${cluster.state}")
  }

  def register(member: Member)(implicit timeout: Timeout = Timeouts.short): Unit = {
    log.info(s"*************** Accept: ${member.address} roles: ${member.roles} members: ${Cluster.get(context.system).state.members.size}")
    if (member.hasRole(Roles.Controller.name)) {
      context.actorSelection(actorPath(member.address, Roles.Controller)).resolveOne() map { ref =>
        addressMapping.addBinding(member.address, ref)
        controllers += ref
      }
    }
    if (member.hasRole(Roles.Worker.name)) {
      context.actorSelection(actorPath(member.address, Roles.Worker)).resolveOne() map { ref =>
        addressMapping.addBinding(member.address, ref)
        workers += ref
      }
    }
    if (member.hasRole(Roles.Lobby.name)) {
      context.actorSelection(actorPath(member.address, Roles.Lobby)).resolveOne() map { ref =>
        addressMapping.addBinding(member.address, ref)
        lobbies += ref
        this.onLobbyJoined(ref.path.address)
        initialLobby.success(ref)
      }
    }
  }

  def unregister(member: Member) = {
    log.info(s"*************** Removing: ${member.address} roles: ${member.roles} members: ${Cluster.get(context.system).state.members.size}")
    if (member.hasRole(Roles.Controller.name)) {
      addressMapping.get(member.address).foreach(_.foreach(ref => controllers -= ref))
      addressMapping -= member.address
    }
    if (member.hasRole(Roles.Worker.name)) {
      addressMapping.get(member.address).foreach(_.foreach(ref => workers -= ref))
      addressMapping -= member.address
    }
    if (member.hasRole(Roles.Lobby.name)) {
      addressMapping.get(member.address).foreach(_.foreach(ref => lobbies -= ref))
      addressMapping -= member.address
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
    case MemberRegistration(role, ref) => {
      addressMapping.addBinding(ref.path.address, ref)
      role match {
        case Roles.Controller =>  controllers += ref
        case Roles.Lobby => lobbies += ref
        case Roles.Worker => workers += ref
      }
      log.info(s"Manual registration $role $ref workers = ${workers.size}")
    }
    case MemberUp(member) => this.register(member)
    case state: CurrentClusterState => {
      // initialize message
      state.members.foreach(this.register)
    }
    case UnreachableMember(member) => {
      log.info("Member detected as unreachable: {}", member)
      this.unregister(member)
    }
    case MemberRemoved(member, previousStatus) => {
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
    }
    case e: MemberEvent => log.info(s"******************** MemberEvent: $e")
  }

  def receiveMsg: Receive

  override def receiveBody: Receive = receiveMsg.orElse(receiveMemberMsg)
}

object MemberBase {
  def actorPath(addr: Address, role: MemberRole) = {
    RootActorPath(addr) / "user" / role.name
  }

}
