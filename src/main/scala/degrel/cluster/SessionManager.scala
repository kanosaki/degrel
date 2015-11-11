package degrel.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._

import scala.collection.mutable
import scala.concurrent.Future

/**
  * Interpreter 1回の実行で使用するデータを保持します
  * 具体的にはIDやロードされたグラフを管理します．
  */
class SessionManager(val lobby: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher
  import messages._

  val nodes = mutable.HashMap[Int, ActorRef]()
  var ctrlr: ActorRef = null
  val controllers = mutable.Seq[ActorRef]()
  var currentId: NodeID = 0

  def allocateInitialNode(newId: NodeID) = {
    implicit val timeout = Timeouts.apiCall
    (lobby ? NodeAllocateRequest(self, NodeInitializeParam(newId))) map {
      case Right(newNode: ActorRef) =>
      {
        println("node allocated")
        newNode
      }
    }
  }

  def prepareInitialNode(): Future[Unit] = {
    if (nodes.isEmpty) {
      currentId += 1
      val nodeId = currentId
      this.allocateInitialNode(nodeId) map { newNode =>
        nodes += nodeId -> newNode
      }
    } else {
      Future {}
    }
  }

  override def receive: Receive = {
    case StartInterpret(msg, controller) if ctrlr != null => {
      log.error(s"Already occupied by $ctrlr")
    }
    case StartInterpret(msg, controller) => {
      println(s"================== START: $msg")
      ctrlr = sender()
      prepareInitialNode() map { _ =>
        val (_, rootNode) = nodes.head
        rootNode ! Push(msg)
      }
    }
    case Fin(msg) => {
      ctrlr ! Fin(msg)
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
