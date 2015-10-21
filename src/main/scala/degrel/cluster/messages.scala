package degrel.cluster

import degrel.core.ID

object messages {

  trait Payload

  // copy graph body, and move its owner
  case class Push(graph: DGraph) extends Payload

  // cell has been stopped
  case class Fin(graph: DGraph) extends Payload


  case class Container(destination: ID, msg: Payload)


  // control
  case object IslandRegistration

  case class Hello()
}
