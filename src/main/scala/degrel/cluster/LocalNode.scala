package degrel.cluster

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import degrel.core.ID
import degrel.engine.{Driver, LocalDriver, RemoteDriver}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}


/**
  * One instance per JVM (= memory space)
  * Context class for Cluster
  */
class LocalNode(system: ActorSystem) {
  import system.dispatcher
  // Timeout for control messages
  implicit val defaultTimeout = Timeout(10.seconds)
  val info = NodeInfo.generate()
  val exchanger = new SpaceExchanger()(this)
  val selfID: Int = 0

  private val driverMapping = mutable.HashMap[Int, LocalDriver]()
  private val neighborNodes = mutable.HashMap[Int, ActorRef]()

  def registerDriver(ownerID: Int, driver: LocalDriver) = {
    if (driverMapping.contains(ownerID)) {
      throw new RuntimeException("Duplicated driver!")
    }
    driverMapping += ownerID -> driver
  }

  def registerNode(nodeID: Int, island: ActorRef) = {
    neighborNodes += nodeID -> island
  }

  def lookupOwner(id: ID): Future[Either[Throwable, Driver]] = {
    import messages._
    if (id.nodeID == 0 || id.nodeID == selfID) {
      Future {
        driverMapping.get(id.ownerID) match {
          case Some(drv) => Right(drv)
          case None => Left(null)
        }
      }
    } else {
      neighborNodes.get(id.nodeID) match {
        case None => Future {
          Left(null)
        }
        case Some(actor) => {
          (actor ? LookupDriver(id)) map {
            case Success(ret) => ret match {
              case Some(ref: ActorRef) => Right(RemoteDriver(ref, this))
              case None => Left(null)
            }
            case Failure(t) => Left(t)
          }
        }
      }
    }
  }
}

object LocalNode {
  def apply(name: String, config: Config) = {
    new LocalNode(ActorSystem(name, config))
  }

  def apply(sys: ActorSystem) = {
    new LocalNode(sys)
  }

  lazy val current = LocalNode("degrel", ConfigFactory.load())
}
