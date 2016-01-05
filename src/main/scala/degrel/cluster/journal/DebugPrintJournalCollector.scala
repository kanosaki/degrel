package degrel.cluster.journal

import degrel.Logger
import degrel.core.NodeID

class DebugPrintJournalCollector(nodeID: NodeID) extends JournalCollector with Logger {
  override def push(item: Journal): Unit = {
    logger.debug(item.toString)
  }
}

