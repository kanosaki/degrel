package degrel.cluster

import akka.actor.Address

/**
  * Worker actor for cluster
  */
class Worker extends MemberBase {
  import messages._
  override def receiveMsg: Receive = {
    case Push(graph) =>
  }

  override protected def onLobbyJoined(addr: Address): Unit = {
    //actorOfRole(addr, Roles.Lobby) ! WorkerRegistration(self)
  }
}

object Worker {
}