package degrel.cluster.journal

import java.util.Calendar

import akka.actor.ActorRef
import degrel.core.NodeID

import scala.concurrent.stm
import scala.concurrent.stm.{InTxn, atomic}


class ClusterJournalCollector(journalCollector: ActorRef, owner: ActorRef, nodeID: NodeID, override val filter: JournalFilter = JournalFilter.default) extends JournalCollector {
  /**
    * Logical timestamp
    */
  protected var nodeTick = stm.Ref(0)
  private val rand = new java.util.Random()

  def uid(): Long = {
    Math.abs(rand.nextLong())
  }

  // call
  def tick(): Unit = atomic { implicit txn =>
    nodeTick += 1
  }

  protected def mkPayload(item: Journal)(implicit txn: InTxn) = {
    JournalPayload(owner.path, nodeID, this.uid(), Calendar.getInstance(), nodeTick.get, item)
  }

  override def push(item: Journal) = atomic { implicit txn =>
    journalCollector ! this.mkPayload(item)
  }
}
