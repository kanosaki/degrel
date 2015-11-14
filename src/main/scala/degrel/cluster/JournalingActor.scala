package degrel.cluster

import java.util.Calendar

import akka.actor.ActorRef

trait JournalingActor extends ActorBase {

  /**
    * Logical timestamp
    */
  protected var nodeTick = 0

  def tick(): Unit = {
    nodeTick += 1
  }

  def nodeID: NodeID

  def journalCollector: ActorRef

  def journal(item: Journal) = {
    journalCollector ! JournalPayload(self, nodeID, Calendar.getInstance(), nodeTick, item)
  }
}
