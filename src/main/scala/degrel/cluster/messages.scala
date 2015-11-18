package degrel.cluster

import akka.actor.{ActorRef, Address}
import degrel.core.{ID, VertexPin}

import scala.concurrent.duration.Duration

object messages {

  trait Payload

  // cell has been stopped
  case class Fin(graph: DGraph) extends Payload

  case class Container(destination: ID, msg: Payload)

  // SessionManager -> Ocean
  case class NodeAllocateRequest(manager: ActorRef, param: NodeInitializeParam)

  case class NewSession()

  case class CloseSession(sess: ActorRef)

  // general request for status
  case class QueryStatus()

  case class LobbyState(active: Boolean)

  case class SessionState()

  case class NodeState()

  case class DriverState(isActive: Boolean)

  case class ControllerState(active: Boolean)

  case class SpawnDriver(graph: DGraph, binding: Seq[Seq[(VertexPin, VertexPin)]], returnTo: VertexPin)

  case class SendGraph(target: ID, graph: DGraph)

  case class WriteVertex(id: ID, graph: DGraph)

  case class RemoveRoot(target: ID)

  case class QueryHeader()

  case class DriverFin(returnTo: VertexPin, graph: DGraph)

  // 2-phase commit
  case class LockForPreCommit(lockingVertices: Seq[VertexPin], requestingTimeout: Duration)

  case class LockResponse(commitId: Long, succeed: Boolean, acceptedTimeout: Duration)

  case class ReleasePostCommit(commitId: Long)


  case class Run(graph: DGraph)


  // Controller -> Session Manager
  case class StartInterpret(cell: DGraph, ctrlr: ActorRef)

  case class FetchJournal(streamReq: Boolean)


  case class Hello()

  // for Island
  // ID of Driver(and Cell)
  // returns Option[ActorRef] (ActorRef == DriverContainer)
  case class LookupDriver(id: ID)

  // Session Controller -> Island
  // Requests island to spawn ClusterNode and return it to Session Controller
  case class SpawnNode(manager: ActorRef, param: NodeInitializeParam)

  case class TellLobby(addr: Address)

  case class JoinLobby(lobby: ActorRef)

  // control
  case class MemberRegistration(role: MemberRole, ref: ActorRef)

}
