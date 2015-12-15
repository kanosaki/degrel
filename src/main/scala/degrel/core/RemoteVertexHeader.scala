package degrel.core

import degrel.Logger
import degrel.cluster.{LocalNode, Timeouts}

import scala.async.Async.{async, await}
import scala.concurrent.Await

class RemoteVertexHeader(_initID: ID, node: LocalNode) extends VertexHeader(_initID) with Logger {
  private var bodyCache: VertexBody = null

  import node.dispatcher

  override def body: VertexBody = {
    if (bodyCache == null) {
      val fut = async {
        logger.debug(s"Fetching on: ${node.selfID} for: ${_initID}")
        await(node.lookup(_initID)) match {
          case Right(v) => v.unhead[VertexBody]
          case Left(msg) => throw msg
        }
      }
      bodyCache = Await.result(fut, Timeouts.short.duration)
    }
    bodyCache
  }

  def isBodyCached: Boolean = bodyCache != null

  override def write(v: Vertex): Unit = {
    node.lookupOwner(_initID) map {
      case Right(drv) => {
        drv.writeVertex(this, v)
      }
      case Left(msg) => {
        throw msg
      }
    }
  }

  override def shallowCopy(): Vertex = ???
}

object RemoteVertexHeader {
  def apply(id: ID, node: LocalNode): RemoteVertexHeader = {
    new RemoteVertexHeader(id, node)
  }
}
