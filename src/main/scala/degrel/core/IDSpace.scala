package degrel.core

import scala.concurrent.stm

trait IDSpace {
  def next(): ID
}

object IDSpace {
  val global = DriverIDSpace(0, 0)
}

case class DriverIDSpace(nodeID: NodeID, ownerID: OwnerID) extends IDSpace {
  private[this] val idCounter = stm.Ref(0)

  def next(): ID = {
    stm.atomic { implicit txn =>
      idCounter.transform(_ + 1)
      GlobalID(nodeID, ownerID, idCounter.get)
    }
  }
}

case class NodeIDSpace(nodeID: NodeID) {
  private[this] val idCounter = stm.Ref(0)

  def next(): DriverIDSpace = {
    stm.atomic { implicit txn =>
      idCounter.transform(_ + 1)
      DriverIDSpace(nodeID, idCounter.get)
    }
  }
}

object NodeIDSpace {
  val global = NodeIDSpace(0)
}
