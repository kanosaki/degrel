package degrel.cluster

import akka.actor.{ActorRef, Address}
import degrel.core.{DriverState, ID, NodeID, VertexPin}

import scala.concurrent.duration.Duration

object messages {

  trait Payload

  // cell has been stopped
  case class Fin(graph: DGraph) extends Payload

  case class Container(destination: ID, msg: Payload)

  // SessionManager -> Ocean
  case class NodeAllocateRequest(manager: ActorRef, param: NodeInitializeParam)

  case class NewSession(controller: ActorRef)

  case class CloseSession(sess: ActorRef)

  // general request for status
  case class QueryStatus()

  case class LobbyState(active: Boolean)

  case class SessionState(nodes: Seq[(NodeID, ActorRef)])

  case class NodeState(id: NodeID, manager: ActorRef, vacancies: Int)

  case class ControllerState(active: Boolean)

  case class SpawnDriver(graph: DGraph, binding: Seq[Seq[(VertexPin, VertexPin)]], returnTo: VertexPin, parent: VertexPin)

  case class SendGraph(target: ID, graph: DGraph)

  case class WriteVertex(id: ID, graph: DGraph)

  case class RemoveRoot(target: ID)

  case class QueryGraph(id: ID, options: QueryOption = QueryOption.None)

  case class LookupDriver(id: ID)

  /**
    * Driver initialization parameter
    * It is permanent as long as hosted in same node.
    */
  case class DriverParameter(root: ID, binding: DBinding, returnTo: VertexPin, parentPin: Option[VertexPin], hostedOn: ActorRef)


  /**
    * Driver current status and statistics.
    */
  case class DriverInfo(originPin: VertexPin, actualID: ID, state: DDriverState)

  case class TellDriverInfo(info: DriverInfo)

  // 2-phase commit
  case class LockForPreCommit(lockingVertices: Seq[VertexPin], requestingTimeout: Duration)

  case class LockResponse(commitId: Long, succeed: Boolean, acceptedTimeout: Duration)

  case class ReleasePostCommit(commitId: Long)


  case class Run(graph: DGraph)


  // Controller -> Session Manager
  case class StartInterpret(cell: DGraph, ctrlr: ActorRef)

  case class FetchJournal(streamReq: Boolean)

  // Session Controller -> Island
  // Requests island to spawn ClusterNode and return it to Session Controller
  case class SpawnNode(manager: ActorRef, param: NodeInitializeParam)

  case class TellLobby(addr: Address)

  // control
  case class MemberRegistration(role: MemberRole, ref: ActorRef)

  case object Halt

}
