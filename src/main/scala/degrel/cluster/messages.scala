package degrel.cluster

import akka.cluster.sharding.ShardRegion
import degrel.core.{Cell, Vertex}

object messages {

  trait Payload

  // copy graph body, and move its owner
  case class Push(cell: Cell) extends Payload

  // cell has been stopped
  case class Fin(v: Vertex) extends Payload


  case class Container(destination: Cell, msg: Payload)


  // control
  case object IslandRegistration

  case class Hello()


  // == cell id ?
  val defaultExtractEntityId: ShardRegion.ExtractEntityId = {
    case msg@Fin(v: Vertex) => (v.id.toString, msg)
    case Container(dst, payload) => (dst.id.toString, payload)
  }

  // == node id ?
  val defaultExtractShardId: ShardRegion.ExtractShardId = {
    case msg@Fin(v: Vertex) => v.id.toString
    case Container(dst, payload) => dst.id.toString
  }

}
