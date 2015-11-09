package degrel.cluster

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import degrel.cluster.messages.{WorkerRegistration, TellLobby}

import scala.collection.mutable
import scala.collection.immutable

trait MemberBase extends Actor with ActorLogging {
  val cluster = Cluster(context.system)
  println(s"CLUSTER BEGIN: ${cluster.selfUniqueAddress} ${cluster.selfAddress} roles: ${cluster.selfRoles}")
  val controllers = mutable.ListBuffer[ActorSelection]()
  val workers = mutable.ListBuffer[ActorSelection]()
  val lobbies = mutable.ListBuffer[ActorSelection]()


  def joinLobby(addr: Address) = {
    //Cluster.get(context.system).joinSeedNodes(immutable.Seq(addr))
    cluster.join(addr)
    actorOfRole(addr, Roles.Lobby) ! WorkerRegistration(self)
    this.onLobbyJoined(addr)
    log.info(s"*************** Joined to Lobby: $addr ${cluster.state}")
  }

  protected def onLobbyJoined(addr: Address) = {}

  def register(member: Member): Unit = {
    log.info(s"*************** Accept: ${member.address} roles: ${member.roles} members: ${Cluster.get(context.system).state.members.size}")
    if (member.hasRole(Roles.Controller.name)) {
      val actors = context.actorSelection(actorPath(member.address, Roles.Controller))
      controllers += actors
    }
    if (member.hasRole(Roles.Worker.name)) {
      val actors = context.actorSelection(actorPath(member.address, Roles.Worker))
      workers += actors
    }
    if (member.hasRole(Roles.Lobby.name)) {
      val actors = context.actorSelection(actorPath(member.address, Roles.Lobby))
      lobbies += actors
    }
  }

  protected def actorPath(addr: Address, role: MemberRole) = {
    RootActorPath(addr) / "user" / role.name
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
      println(s"============== STATE: $state")
      state.members.foreach(this.register)
    }
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
    case e: MemberEvent => log.info(s"******************** MemberEvent: $e")
  }

  def receiveMsg: Receive

  override def receive: Receive = receiveMsg.orElse(receiveMemberMsg)
}
