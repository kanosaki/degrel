package degrel.core

import degrel.cluster.LocalNode

import scala.concurrent.stm


trait ID extends Comparable[ID] with Serializable {
  protected def squeeze: Int

  def compareTo(o: ID): Int = {
    val nodeCmp = this.nodeID.compareTo(o.nodeID)
    if (nodeCmp != 0) {
      return nodeCmp
    }
    val ownerCmp = this.ownerID.compareTo(o.ownerID)
    if (ownerID != 0) {
      return ownerCmp
    }
    this.localID.compareTo(o.localID)
  }

  def localID: Int

  def ownerID: Int

  def nodeID: Int

  def globalize(implicit node: LocalNode): GlobalID

  def hasSameOwner(other: ID): Boolean = {
    (other.nodeID == 0 || other.nodeID == this.nodeID) && other.ownerID == this.ownerID
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case null => false
      case i: ID if i.localID == this.localID && i.ownerID == this.ownerID && i.nodeID == this.nodeID => true
      case _ => false
    }
  }

  override def toString: String = {
    (nodeID, ownerID) match {
      case (0, 0) => f"$localID%x"
      case (0, _) => f"$ownerID%x.$localID%x"
      case (_, _) => f"$nodeID%x.$ownerID%x.$localID%x"
    }
  }

  def shorten: String = {
    val moddedID = localID % 1000
    f"$moddedID%02x"
  }

  def withOwner(owner: Vertex): ID = {
    GlobalID(owner.id.nodeID, owner.id.ownerID, this.localID)
  }

  def isFree: Boolean = {
    this.nodeID == 0 || this.ownerID == 0
  }

  def canOwnBy(v: Vertex): Boolean = {
    (this.nodeID == 0 || v.id.nodeID == this.nodeID) && (this.ownerID == 0 || v.id.ownerID == this.ownerID)
  }
}

object ID {
  private[this] val idCounter = stm.Ref(0)
  private[this] val cellIdCounter = stm.Ref(0)

  private def nextID(cellID: Int): ID = {
    stm.atomic { implicit txn =>
      idCounter.transform(_ + 1)
      LocalID(cellID, idCounter.get)
    }
  }

  def nextLocalCellID(): ID = {
    stm.atomic { implicit txn =>
      cellIdCounter.transform(_ + 1)
      idCounter.transform(_ + 1)
      LocalID(cellIdCounter.get, idCounter.get)
    }
  }

  def NA: ID = NotAssignedID

  def autoAssign(owner: Vertex): ID = this.nextID(owner.id.ownerID)

  def nextLocalVertexID(): ID = {
    stm.atomic { implicit txn =>
      idCounter.transform(_ + 1)
      FreeID(idCounter.get)
    }
  }
}

case object NotAssignedID extends ID {
  protected def squeeze: Int = throw new Exception("Couldnt compare NotAssignedID")

  override def globalize(implicit node: LocalNode): GlobalID = GlobalID(0, 0, 0)

  override def localID: Int = 0

  override def ownerID: Int = 0

  override def nodeID: Int = 0

  override def equals(obj: scala.Any): Boolean = false

  override def withOwner(owner: Vertex): ID = ID.autoAssign(owner)

}

case class FreeID(localID: Int) extends ID {
  override protected def squeeze: Int = localID

  override def ownerID: Int = 0

  override def nodeID: Int = 0

  override def globalize(implicit node: LocalNode): GlobalID = GlobalID(node.selfID, 0, localID)
}

object GlobalID {
  val HOST_BITS: Long = 0xFFFFFFFF00000000l
  val ELEM_BITS: Long = 0x00000000FFFFFFFFl
}

case class GlobalID(nodeID: Int, ownerID: Int, localID: Int) extends ID {
  override protected def squeeze: Int = localID

  override def globalize(implicit node: LocalNode): GlobalID = {
    if (node.selfID == this.nodeID) {
      this
    } else {
      GlobalID(node.selfID, this.ownerID, this.localID)
    }
  }
}

case class LocalID(ownerID: Int, localID: Int) extends ID {
  def squeeze: Int = localID

  override def globalize(implicit node: LocalNode): GlobalID = GlobalID(node.selfID, this.ownerID, this.localID)

  override def nodeID: Int = 0
}
