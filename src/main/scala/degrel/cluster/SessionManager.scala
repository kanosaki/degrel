package degrel.cluster

import akka.actor.{ActorRef, Props}
import akka.pattern._
import degrel.core.Label
import degrel.engine.namespace.Repository
import degrel.engine.rewriting.Binding
import degrel.engine.{RemoteDriver, Chassis, LocalDriver}

import scala.collection.mutable
import scala.concurrent.Future

/**
  * Interpreter 1回の実行で使用するデータを保持します
  * 具体的にはIDやロードされたグラフを管理します．
  */
class SessionManager(val lobby: ActorRef) extends ActorBase {

  import context.dispatcher
  import messages._

  val nodes = mutable.HashMap[NodeID, ActorRef]()
  val journals = mutable.ListBuffer[JournalPayload]()
  var ctrlr: ActorRef = null
  val controllers = mutable.Seq[ActorRef]()

  // first node id == 2
  var currentId: NodeID = 1
  var streamJournal = false
  val journal = JournalAdapter(self, self, 1)
  var repo = Repository()
  var chassis: Chassis = null
  val localNode = LocalNode(context.system, journal, repo)
  localNode.selfID = 1


  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    nodes.foreach { case (_, ref) =>
      context.stop(ref)
    }
  }

  def requestNewNode(newId: NodeID): Future[Either[Throwable, ActorRef]] = {
    implicit val timeout = Timeouts.apiCall
    (lobby ? NodeAllocateRequest(self, NodeInitializeParam(newId))) map {
      case Right(newNode: ActorRef) => {
        Right(newNode)
      }
      case Left(msg: Throwable) => {
        Left(msg)
      }
    }
  }

  def allocateNextNode(): Future[Either[Throwable, ActorRef]] = {
    currentId += 1
    val nodeId = currentId
    this.requestNewNode(nodeId) map {
      case Right(newNode) => {
        nodes += nodeId -> newNode
        Right(newNode)
      }
      case Left(v) => Left(v)
    }
  }

  def allocateMaxNodes(): Future[Unit] = {
    this.allocateNextNode() flatMap {
      case Right(_) => this.allocateMaxNodes()
      case Left(_) => Future {}
    }
  }

  def prepareInitialNode(): Future[Unit] = {
    if (nodes.isEmpty) {
      this.allocateNextNode() map {
        case Left(msg) => {
          log.error("Cannot allocate initial node")
        }
        case _ =>
      }
    } else {
      Future {}
    }
  }

  override def receiveBody: Receive = {
    case QueryStatus() => {
      sender() ! SessionState(nodes.toSeq)
    }
    case QueryGraph(id, options) => {
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
          println("Cannot find owner! please select proper node.")
          origin ! Left(msg)
        }
      }
    }
    case LookupDriver(id) => {
      println(s"LookupDriver on ${localNode.selfID}(Manager) $id")
      val origin = sender()
      localNode.lookupOwnerLocal(id) match {
        case Right(drv) => origin ! Right(drv.param(self))
        case Left(msg) => origin ! Left(msg)
      }
    }
    case StartInterpret(msg, controller) if ctrlr != null => {
      log.error(s"Already occupied by $ctrlr")
    }
    case StartInterpret(msg, controller) => {
      ctrlr = sender()
      val unpacked = localNode.exchanger.unpack(msg)
      // own vertices as program
      repo.register(Label.N.main, localNode.spawnLocally(unpacked, Binding.empty(), null, null))
      chassis = Chassis.create(repo)
      localNode.registerDriver(chassis.main.header.id.ownerID, chassis.main.asInstanceOf[LocalDriver])
      val packed = localNode.exchanger.packAll(unpacked, move = true)

      allocateMaxNodes() map { _ =>
        val (_, rootNode) = nodes.head
        rootNode ! Run(packed)
      }
    }
    case TellDriverInfo(info: DriverInfo) => {
      println(s"TellDriverInfo $info")
      // Remote driver state is updated.
      localNode.lookupOwnerLocal(info.origin) match {
        case Right(drv: RemoteDriver) => {
          drv.remoteUpdated(info)
        }
        case _ => {
          log.warning(s"Cannot update info $info")
        }
      }
    }
    case Fin(msg) => {
      ctrlr ! Fin(msg)
    }
    case jp: JournalPayload => {
      journals += jp
      if (streamJournal) {
        ctrlr ! jp
      }
    }
    case FetchJournal(streamReq) => {
      println(s"${sender()} $ctrlr")
      sender() ! Right(journals.toVector)
    }
  }
}

object SessionManager {
  def props(lobby: ActorRef): Props = Props(new SessionManager(lobby))

  sealed trait State

  case object Idle extends State

  case object Working extends State

  case object Finished extends State

}

case class NodeInitializeParam(id: NodeID)
