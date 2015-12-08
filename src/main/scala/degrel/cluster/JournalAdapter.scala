package degrel.cluster

import java.util.Calendar

import akka.actor.ActorRef
import degrel.Logger
import degrel.core.NodeID

import scala.concurrent.stm
import scala.concurrent.stm.{InTxn, atomic}

trait JournalAdapter {
  def apply(item: Journal): Unit
}

class ClusterJournalAdapter(journalCollector: ActorRef, owner: ActorRef, nodeID: NodeID) extends JournalAdapter {
  /**
    * Logical timestamp
    */
  protected var nodeTick = stm.Ref(0)

  // call
  def tick(): Unit = atomic { implicit txn =>
    nodeTick += 1
  }

  protected def mkPayload(item: Journal)(implicit txn: InTxn) = {
    JournalPayload(owner, nodeID, Calendar.getInstance(), nodeTick.get, item)
  }

  override def apply(item: Journal) = atomic { implicit txn =>
    journalCollector ! this.mkPayload(item)
  }
}

class LoggingJournalAdapter(nodeID: NodeID) extends JournalAdapter with Logger {
  override def apply(item: Journal): Unit = {
    logger.debug(item.toString)
  }
}

object JournalAdapter {
  def loggingAdapter(nodeID: NodeID): LoggingJournalAdapter = {
    new LoggingJournalAdapter(nodeID)
  }

  def apply(collector: ActorRef, owner: ActorRef, nodeID: NodeID) = {
    new ClusterJournalAdapter(collector, owner, nodeID)
  }
}
