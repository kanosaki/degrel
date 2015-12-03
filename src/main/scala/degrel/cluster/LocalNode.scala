package degrel.cluster

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import degrel.core.{ID, Label, Vertex, VertexPin}
import degrel.engine.namespace.Repository
import degrel.engine.rewriting.Binding
import degrel.engine.sphere.Sphere
import degrel.engine.{Chassis, Driver, LocalDriver}

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * One instance per JVM (= memory space)
  * Context class for Cluster
  */
class LocalNode(system: ActorSystem, journal: JournalAdapter, repo: Repository) {

  implicit val dispatcher = system.dispatcher

  // Timeout for control messages
  implicit val defaultTimeout = Timeout(10.seconds)
  val exchanger = new SpaceExchanger()(this)
  val driverFactory = ClusterDriverFactory(this)
  val chassis = Chassis(repo, driverFactory)

  val driverCache = mutable.HashMap[ID, Driver]()

  var selfID: NodeID = 0

  private val driverMapping = mutable.HashMap[Int, LocalDriver]()
  private val neighborNodes = mutable.HashMap[Int, RemoteNode]()

  def registerDriver(ownerID: Int, driver: LocalDriver) = {
    if (chassis.verbose) {
      println(s"Registering Driver on $selfID ID: ${driver.id} cell: ${driver.header.pp}")
    }
    driverMapping += ownerID -> driver
  }

  def registerNode(nodeID: Int, node: ActorRef) = {
    neighborNodes += nodeID -> RemoteNode(nodeID, node, this)
  }

  def isLocalId(id: ID): Boolean = {
    id.nodeID == 0 || id.nodeID == selfID
  }

  def lookupOwnerLocal(id: ID): Either[Throwable, Driver] = {
    if (isLocalId(id)) {
      driverMapping.get(id.ownerID) match {
        case Some(drv) => Right(drv)
        case None => Left(new RuntimeException(s"Lookup failed for $id No such driver in $selfID(drivers: ${driverMapping.values}"))
      }
    } else {
      Left(new RuntimeException("not local id!"))
    }
  }

  def lookupOwner(id: ID): Future[Either[Throwable, Driver]] = {
    //println(s"LookupOwner on: $selfID $id")
    if (isLocalId(id)) {
      async {
        this.lookupOwnerLocal(id)
      }
    } else {
      driverCache.get(id) match {
        case Some(drv) => Future {
          Right(drv)
        }
        case None => {
          neighborNodes.get(id.nodeID) match {
            case None => async {
              Left(new RuntimeException(s"There is no such node $id"))
            }
            case Some(node) => async {
              await(node.lookupOwner(id)) match {
                case Right(drv) => {
                  driverCache += id -> drv
                  Right(drv)
                }
                case other => other
              }
            }
          }
        }
      }
    }
  }

  def lookup(id: ID): Future[Either[Throwable, Vertex]] = async {
    if (chassis.verbose) {
      println(s"Lookup on: $selfID for $id")
    }
    await(this.lookupOwner(id)) match {
      case Right(drv) => {
        drv.getVertex(id) match {
          case Some(v) => Right(v)
          case None => Left(new RuntimeException("Owner foudn but vertex not found"))
        }
      }
      case Left(msg) => {
        msg.printStackTrace()
        Left(msg)
      }
    }
  }

  def getSphere(driver: Driver): Sphere = {
    ???
  }

  // designates a node to spawn on, and executes spawning.
  def spawnSomewhere(cell: Vertex, binding: Binding, returnTo: VertexPin, parent: Driver): Future[Either[Throwable, Driver]] = {
    if (chassis.verbose) {
      println(s"spawnSomewhere on: $selfID $cell")
    }
    if (neighborNodes.isEmpty) {
      async {
        Right(this.spawnLocally(cell, binding, returnTo, parent))
      }
    } else {
      println(neighborNodes)
      val (_, spawnNode) = neighborNodes.filter(_._1 > 1).head
      spawnNode.spawn(cell, binding, returnTo)
    }
  }

  def spawnLocally(cell: Vertex, binding: Binding, returnTo: VertexPin, parent: Driver): Driver = {
    journal(Journal.CellSpawn(cell.id, selfID))
    val drv = chassis.createDriver(cell, parent)
    if (chassis.verbose) {
      println(s"LOCAL SPAWN on: $selfID $cell $binding $returnTo $parent ID: ${drv.id}")
    }
    drv
  }

  def nextCellID(): ID = {
    ID.nextLocalCellID().globalize(this)
  }

  override def toString: String = {
    s"<LocalNode ID: $selfID drivers: ${driverMapping.size} neighbors: ${neighborNodes.size}>"
  }
}

object LocalNode {
  private lazy val debugSys = ActorSystem("DebugSys")

  def apply() = {
    new LocalNode(debugSys, JournalAdapter.loggingAdapter(0), Repository())
  }

  def apply(sys: ActorSystem, journal: JournalAdapter, repo: Repository) = {
    new LocalNode(sys, journal, repo)
  }

  def apply(sys: ActorSystem) = {
    new LocalNode(sys, JournalAdapter.loggingAdapter(0), Repository())
  }
}
