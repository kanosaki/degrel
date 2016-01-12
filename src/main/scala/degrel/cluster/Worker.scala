package degrel.cluster

import akka.actor.{ActorRef, Address, Props}

/**
  * Worker actor for cluster
  */
class Worker(lobbyAddr: Address, lobbyRef: ActorRef) extends MemberBase {

  import messages._

  if (lobbyRef != null) {
    this.joinLobby(lobbyRef)
  } else {
    if (lobbyAddr != null) {
      this.joinLobby(lobbyAddr)
    } else {
      throw new RuntimeException("no join method")
    }
  }


  def spawnNode(manager: ActorRef, param: NodeInitializeParam): ActorRef = {
    context.actorOf(SessionNode.props(self, manager, param))
  }

  override def receiveMsg: Receive = {
    case SpawnNode(manager, param) => {
      sender() ! Right(spawnNode(manager, param))
    }
  }

  override def role: MemberRole = Roles.Worker
}

object Worker {
  def props(lobbyAddr: Address, lobbyRef: ActorRef) = Props(new Worker(lobbyAddr, lobbyRef))
}