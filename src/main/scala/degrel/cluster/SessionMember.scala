package degrel.cluster

import degrel.cluster.messages._
import degrel.engine.{Chassis, RemoteDriver}

import scala.async.Async.{async, await}

abstract class SessionMember extends ActorBase {

  import context.dispatcher

  def localNode: LocalNode

  def chassis: Chassis

  def receiveMsg: Receive

  override def receiveBody: Receive = receiveMsg.orElse(receiveCommon)

  def receiveCommon: Receive = {
    case QueryGraph(id, options) => {
      log.debug(s"QueryGraph on: ${localNode.selfID} for $id options: $options")
      val origin = sender()
      localNode.lookupOwnerLocal(id) match {
        case Right(drv) => {
          drv.getVertex(id) match {
            case Some(v) => {
              val dGraph = localNode.exchanger.packForQuery(v, options)
              origin ! Right(dGraph)
            }
            case None => origin ! Left(new RuntimeException(s"Graph not found for $id"))
          }
        }
        case Left(msg) => {
          log.error("Cannot find owner! please select proper node.")
          origin ! Left(msg)
        }
      }
    }
    case SendGraph(target, graph) => {
      log.debug(s"SendGraph on: ${localNode.selfID} $target $graph")
      localNode.lookupOwner(target) map {
        case Right(drv) => {
        }
        case Left(err) => {
          sender() ! "Cannto send data"
        }
      }
    }
    case LookupDriver(id) => {
      log.debug(s"LookupDriver on ${localNode.selfID} $id")
      val origin = sender()
      localNode.lookupOwnerLocal(id) match {
        case Right(drv) => origin ! Right(drv.param(self))
        case Left(msg) => origin ! Left(msg)
      }
    }
    case TellDriverInfo(info: DriverInfo) => {
      log.debug(s"TellDriverInfo $info")
      // Remote driver state is updated.
      localNode.remoteMapping.get(info.originPin.id) match {
        case Some(drv: RemoteDriver) => {
          drv.remoteUpdated(info)
        }
        case other => {
          log.warning(s"Cannot update info on: ${localNode.selfID} $info, $other ${localNode.remoteMapping}")
        }

      }
    }
    case WriteVertex(id, graph) => {
      log.debug(s"WriteVertex on: ${localNode.selfID} to: $id graph: $graph")
      async {
        val driver = await(localNode.lookupOwner(id)) match {
          case Right(drv) => drv
          case Left(msg) => {
            log.error(msg, s"Cannot write vertex to $id graph: No DRIVER!: $graph")
            throw msg
          }
        }
        val target = driver.getVertex(id) match {
          case Some(v) => v
          case None => {
            log.error(s"No such vertex id: $id driver: $driver")
            throw new RuntimeException(s"No such vertex id: $id driver: $driver")
          }
        }
        val unpacked = localNode.exchanger.unpack(graph)
        println(s"WriteVertex on: ${localNode.selfID} to: $id graph: $graph")
        driver.writeVertex(target.asHeader, unpacked)
        println(driver.header.pp)
      }
    }
  }
}
