package degrel.cluster

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import degrel.Logger
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
class LocalNode(system: ActorSystem, journal: JournalAdapter, repo: Repository) extends Logger {

  implicit val dispatcher = system.dispatcher

  // Timeout for control messages
  implicit val defaultTimeout = Timeout(10.seconds)
  val exchanger = new SpaceExchanger()(this)
  val driverFactory = ClusterDriverFactory(this)
  val chassis = Chassis(repo, driverFactory)

  val driverCache = mutable.HashMap[ID, Driver]()

  var selfID: NodeID = 0

  private val driverMapping = mutable.HashMap[Int, LocalDriver]()

  /**
    * Neighbor nodes. Node 1 is `SessionManager`, and others are `SessionNode`.
    * And self node won't contain.
    */
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

  /**
    * Lookup a driver from local node.
    * @param id A local ID of Driver which looking up for.
    */
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

  /**
    * Lookups a Driver from session space.
    * @param id ID of driver wihch looking up for. ID can be local or remote.
    * @return A Driver(might be RemoteDriver) if found, or Throwable if some problem occurs.
    */
  def lookupOwner(id: ID): Future[Either[Throwable, Driver]] = {
    if (id.ownerID == 0) {
      logger.warn(s"Looking up Anonymous node vertex ID: $id")
    }
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

  /**
    * Lookup a vertex from session space.
    * @param id ID of looking up for. ID can be local or remote.
    * @return A Vertex(might be RemoteVertex) or Throwable if some problem occurs.
    */
  def lookup(id: ID): Future[Either[Throwable, Vertex]] = async {
    if (id.ownerID == 0) {
      logger.warn(s"Looking up Anonymous node vertex ID: $id")
    }
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
  /**
    * Spawns a driver somewhere else, spawning node will be designated by cluster driver.
    * @param cell A cell to spawn.
    * @param binding Binding environment of spawning cell.
    * @param returnTo A return point of spawning cell.
    * @param parent A primary parent of spawning driver.
    * @return A Driver reference of spawned driver, or Throwable when some problem occurs.
    */
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

  /**
    * Spawns a driver in this node.
    */
  def spawnLocally(cell: Vertex, binding: Binding, returnTo: VertexPin, parent: Driver): Driver = {
    journal(Journal.CellSpawn(cell.id, selfID))
    val drv = chassis.createDriver(cell, parent)
    if (chassis.verbose) {
      println(s"LOCAL SPAWN on: $selfID $cell $binding $returnTo $parent ID: ${drv.id}")
    }
    drv
  }

  /**
    * Returns a ID for new cell.
    */
  def nextCellID(): ID = {
    ID.nextLocalCellID().globalize(this)
  }

  override def toString: String = {
    s"<LocalNode ID: $selfID drivers: ${driverMapping.size} neighbors: ${neighborNodes.size}>"
  }
}

object LocalNode {
  private lazy val debugSys = ActorSystem("DebugSys")

  /**
    * __Debugging Overload!__ <br />
    * Creates a new LcoalNode with "DebugSys" `ActorSystem` and a logging `JournalAdapter`
    */
  def apply() = {
    new LocalNode(debugSys, JournalAdapter.loggingAdapter(0), Repository())
  }

  def apply(sys: ActorSystem, journal: JournalAdapter, repo: Repository) = {
    new LocalNode(sys, journal, repo)
  }

  /**
    * __Debugging Overload!__ <br />
    * Creates a new LcoalNode with given `ActorSystem` and a logging `JournalAdapter`
    */
  def apply(sys: ActorSystem) = {
    new LocalNode(sys, JournalAdapter.loggingAdapter(0), Repository())
  }
}
