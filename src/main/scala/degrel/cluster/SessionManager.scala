package degrel.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern._
import degrel.cluster.messages.NodeAllocateRequest

import scala.collection.mutable
import scala.util.Success

/**
  * Interpreter 1回の実行で使用するデータを保持します
  * 具体的にはIDやロードされたグラフを管理します．
  */
class SessionManager(val ocean: ActorRef, initialNode: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  val nodes = mutable.HashMap[Int, ActorRef]()
  val controllers = mutable.Seq[ActorRef]()

  def acceptNewNode(node: ActorRef) = {
    // generate new ID
    // send initialize message to node
  }

  def allocateInitialNode() = {
    implicit val timeout = Timeouts.apiCall
    (ocean ? NodeAllocateRequest()).onComplete {
      case Success(retNodes: Seq[ActorRef]) => retNodes.foreach(acceptNewNode)
    }
  }

  import messages._
  override def receive: Receive = {
    case StartInterpret(msg) => {

    }
  }
}
