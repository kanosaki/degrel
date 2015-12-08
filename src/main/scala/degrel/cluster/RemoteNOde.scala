package degrel.cluster

import akka.actor.ActorRef
import akka.pattern.ask
import degrel.Logger
import degrel.cluster.messages.{DriverParameter, LookupDriver, SpawnDriver}
import degrel.core.{NodeID, ID, Vertex, VertexPin}
import degrel.engine.rewriting.Binding
import degrel.engine.{Driver, RemoteDriver}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

class RemoteNode(val selfID: NodeID, val ref: ActorRef, hostedOn: LocalNode)(implicit ec: ExecutionContext) extends Logger {
  // TODO: Cache remote drivers
  /**
    * Confirms the driver exists.
    */
  def lookupOwner(id: ID): Future[Either[Throwable, Driver]] = async {
    logger.debug(s"LookupOwner(Remote) on: ${hostedOn.selfID} for $id --> $selfID")
    implicit val timeout = Timeouts.short
    await(ref ? LookupDriver(id)) match {
      case Right(info: DriverParameter) => {
        logger.debug(s"Lookup for $id Done on: $selfID remote: $info")
        try {
          val i = RemoteDriver.remoteDriver(info, hostedOn)
          Right(i)
        } catch {
          case e: Throwable => Left(e)
        }
      }
      case Left(msg: Throwable) => Left(msg)
    }
  }

  def spawn(cell: Vertex, binding: Binding, returnTo: VertexPin, parent: Driver): Future[Either[Throwable, RemoteDriver]] = async {
    logger.debug(s"REMOTE SPAWN $cell $binding --> $selfID")
    implicit val timeout = Timeouts.short
    val graph = hostedOn.exchanger.packAll(cell, move = true)
    val res = await(ref ? SpawnDriver(graph, Seq(), returnTo, parent.header.pin))
    logger.debug(s"REMOTE SPAWN DONE $res")
    res match {
      case Right(info: DriverParameter) => {
        val drv = RemoteDriver.localPhantom(cell.asHeader, info, hostedOn)
        Right(drv)
      }
      case Left(msg: Throwable) => Left(msg)
    }
  }

  override def toString: String = {
    s"<RemoteNode ID: $selfID on: ${hostedOn.selfID} remote: $ref>"
  }
}

object RemoteNode {
  def apply(id: NodeID, ref: ActorRef, hostedOn: LocalNode)(implicit ec: ExecutionContext): RemoteNode = {
    new RemoteNode(id, ref, hostedOn)
  }
}
