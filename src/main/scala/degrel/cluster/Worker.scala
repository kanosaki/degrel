package degrel.cluster

import akka.actor.{ActorRef, Address}

/**
  * Worker actor for cluster
  */
class Worker extends MemberBase {

  import messages._

  def spawnNode(manager: ActorRef, param: NodeInitializeParam): ActorRef = {
    context.actorOf(SessionNode.props(self, manager, param), name = "node")
  }

  override def receiveMsg: Receive = {
    case SpawnNode(manager, param) => {
      sender() ! Right(spawnNode(manager, param))
    }
  }

  override protected def onLobbyJoined(addr: Address): Unit = {
  }
}

object Worker {
}