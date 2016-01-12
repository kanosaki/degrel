package degrel.cluster

import akka.actor.{ActorRef, Props}
import akka.pattern._
import degrel.cluster.journal.Journal.{Load, SessionFinished}
import degrel.cluster.journal.{JournalCollector, JournalPayload, JsonJournalSink}
import degrel.core.{Label, NodeID, NodeIDSpace}
import degrel.engine.namespace.Repository
import degrel.engine.rewriting.Binding
import degrel.engine.{Chassis, LocalDriver}

import scala.async.Async.{async, await}
import scala.collection.mutable
import scala.concurrent.Future

/**
  * Interpreter 1回の実行で使用するデータを保持します
  * 具体的にはIDやロードされたグラフを管理します．
  */
class SessionManager(val lobby: ActorRef) extends SessionMember {

  import context.dispatcher
  import messages._

  val nodes = mutable.HashMap[NodeID, ActorRef]()
  val journals = mutable.ListBuffer[JournalPayload]()
  var ctrlr: ActorRef = null
  val controllers = mutable.Seq[ActorRef]()

  // first node id == 2
  var currentId: NodeID = 1
  var streamJournal = false
  val journal = JournalCollector(self, self, 1)
  val journalSink = JsonJournalSink("log/journal")
  var repo = Repository()
  var chassis: Chassis = null
  val localNode = LocalNode(context.system, journal, repo, NodeIDSpace(1))


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
        localNode.registerNode(nodeId, newNode)
        Right(newNode)
      }
      case Left(v) => {
        Left(v)
      }
    }
  }

  def allocateMaxNodes(): Future[Unit] = {
    this.allocateNextNode() flatMap {
      case Right(_) => this.allocateMaxNodes()
      case Left(_) => Future { }
    }
  }

  def broadcastStatus(): Unit = {
    this.nodes.foreach { case (_, nodeRef) =>
      nodeRef ! SessionState(this.nodes.toSeq)
    }
  }

  override def receiveMsg: Receive = {
    case QueryStatus() => {
      sender() ! SessionState(nodes.toSeq)
    }
    case StartInterpret(msg, controller) if ctrlr != null => {
      log.error(s"Already occupied by $ctrlr")
    }
    case StartInterpret(msg, controller) => {
      ctrlr = sender()
      async {
        await(this.allocateMaxNodes())
        this.broadcastStatus()
        this.logSessionStatus()
        journal(Load(localNode.selfID, msg, "__main__"))
        val unpacked = localNode.exchanger.unpack(msg)
        // own vertices as program
        repo.register(Label.N.main, localNode.spawnLocally(unpacked, Binding.empty(), null, null))
        chassis = Chassis.create(repo, localNode)
        localNode.registerDriver(chassis.main.header.id.ownerID, chassis.main.asInstanceOf[LocalDriver])
        val startTime = System.currentTimeMillis()
        chassis.main.start()
        async {
          val result = await(chassis.main.finValue.future)
          log.info(s"RUNNING FINISHED: $result")
          val packed = localNode.exchanger.packAll(result)
          journal(SessionFinished(System.currentTimeMillis() - startTime))
          ctrlr ! messages.Fin(packed)
        }
      }
    }
    case Fin(msg) => {
      ctrlr ! Fin(msg)
    }
    case jp: JournalPayload => {
      journals += jp
      journalSink.sink(jp)
      if (streamJournal) {
        ctrlr ! jp
      }
    }
    case FetchJournal(streamReq) => {
      streamJournal = streamReq
      sender() ! Right(journals.toVector)
    }
  }

  def logSessionStatus(): Unit = {
    log.info("------------- Session Status Report ----------------")
    log.info("Nodes:")
    log.info(s"1(Manager): $self")
    nodes.foreach { case (id, ref) =>
      log.info(s"$id: $ref")
    }
    log.info("----------------------------------------------------")
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
