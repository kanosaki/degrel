package degrel.cluster.journal

import akka.actor.ActorRef
import degrel.core.NodeID

import scala.reflect.runtime.universe

trait JournalCollector {
  def filter: JournalFilter = JournalFilter.default

  def push(item: Journal): Unit

  def apply[T <: Journal : universe.TypeTag](item: => T) = {
    if (filter.byType(universe.typeTag[T].tpe) && filter.byValue(item)) {
      this.push(item)
    }
  }
}

object JournalCollector {
  def loggingAdapter(nodeID: NodeID): DebugPrintJournalCollector = {
    new DebugPrintJournalCollector(nodeID)
  }

  def apply(collector: ActorRef, owner: ActorRef, nodeID: NodeID) = {
    new ClusterJournalCollector(collector, owner, nodeID, Journal.Filters.all)
  }
}

