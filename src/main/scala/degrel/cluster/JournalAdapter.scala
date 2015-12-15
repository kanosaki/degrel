package degrel.cluster

import java.util.Calendar

import akka.actor.ActorRef
import degrel.Logger
import degrel.core.NodeID

import scala.collection.mutable
import scala.concurrent.stm
import scala.concurrent.stm.{InTxn, atomic}
import scala.reflect.runtime.universe

trait JournalAdapter {
  def filter: JournalFilter = JournalFilter.default

  def push(item: Journal): Unit

  def apply[T <: Journal : universe.TypeTag](item: => T) = {
    if (filter.byType(universe.typeTag[T].tpe) && filter.byValue(item)) {
      this.push(item)
    }
  }
}

trait JournalFilter {
  def byType(journalType: universe.Type): Boolean

  def byValue(journalValue: Journal): Boolean
}

object JournalFilter {
  val default = new ThroughJournalFilter()
}

class ClusterJournalAdapter(journalCollector: ActorRef, owner: ActorRef, nodeID: NodeID, override val filter: JournalFilter = JournalFilter.default) extends JournalAdapter {
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

  override def push(item: Journal) = atomic { implicit txn =>
    journalCollector ! this.mkPayload(item)
  }
}

class LoggingJournalAdapter(nodeID: NodeID) extends JournalAdapter with Logger {
  override def push(item: Journal): Unit = {
    logger.debug(item.toString)
  }
}

object JournalAdapter {
  def loggingAdapter(nodeID: NodeID): LoggingJournalAdapter = {
    new LoggingJournalAdapter(nodeID)
  }

  def apply(collector: ActorRef, owner: ActorRef, nodeID: NodeID) = {
    new ClusterJournalAdapter(collector, owner, nodeID, Journal.Filters.all)
  }
}

class ThroughJournalFilter extends JournalFilter {
  override def byType(journalType: universe.Type): Boolean = true

  override def byValue(journalValue: Journal): Boolean = true
}

class WhiteListJournalFilter extends JournalFilter {
  private val acceptableTypeSet: mutable.Set[String] = mutable.HashSet()

  def accept[T <: Journal : universe.TypeTag]: this.type = {
    acceptableTypeSet += universe.typeOf[T].toString
    this
  }

  override def byType(journalType: universe.Type): Boolean = {
    acceptableTypeSet.contains(journalType.toString)
  }

  override def byValue(journalValue: Journal): Boolean = true
}
