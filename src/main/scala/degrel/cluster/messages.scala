package degrel.cluster

import akka.actor.{ActorRef, Address}
import degrel.core.ID

object messages {

  trait Payload

  // copy graph body, and move its owner
  case class Push(graph: DGraph) extends Payload

  // cell has been stopped
  case class Fin(graph: DGraph) extends Payload


  case class Container(destination: ID, msg: Payload)


  // control
  case class WorkerRegistration(workerRef: ActorRef)

  // SessionManager -> Ocean
  case class NodeAllocateRequest()

  case class NewSession()

  case class SendGraph(target: ID, graph: DGraph)

  case class Run(graph: DGraph)


  // Controller -> Session Manager
  case class StartInterpret(cell: DGraph)

  case class Hello()

  // for Island
  // ID of Driver(and Cell)
  // returns Option[ActorRef] (ActorRef == DriverContainer)
  case class LookupDriver(id: ID)

  // Session Controller -> Island
  // Requests island to spawn ClusterNode and return it to Session Controller
  case class SpawnNode(id: NodeID)

  case class TellLobby(addr: Address)
}
