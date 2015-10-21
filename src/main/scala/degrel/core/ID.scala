package degrel.core

import degrel.cluster.LocalNode

import scala.concurrent.stm


trait ID extends Comparable[ID] with Serializable {
  def shorten: String = {
    this.toString.substring(0, 3)
  }

  protected def squeeze: Int

  def compareTo(o: ID) = {
    this.squeeze - o.squeeze
  }

  def autoValidate: ID

  def globalize(implicit node: LocalNode): GlobalID
}

object ID {
  private[this] val idCounter = stm.Ref(-1)

  private def nextID: ID = {
    stm.atomic {
                 implicit txn =>
                   idCounter.transform(_ + 1)
                   LocalID(idCounter.get)
               }
  }

  def NA: ID = NotAssignedID

  def autoAssign: ID = this.nextID

}

case object NotAssignedID extends ID {
  protected def squeeze: Int = throw new Exception("Couldnt compare NotAssignedID")

  def autoValidate: ID = ID.autoAssign

  override def globalize(implicit node: LocalNode): GlobalID = this.autoValidate.globalize(node)
}


//case class GlobalID(numID: Long) extends AnyVal with ID {
//  override def toString: String = {
//    f"$numID%x"
//  }

//  override def shorten: String = {
//    val moddedID = numID % 1000
//    f"$moddedID%02x"
//  }

//  def squeeze: Long = numID

//  def autoValidate: ID = this
//}

object GlobalID {
  val HOST_BITS: Long = 0xFFFFFFFF00000000l
  val ELEM_BITS: Long = 0x00000000FFFFFFFFl
}

case class GlobalID(hostID: Int, localID: Int) extends ID {
  override protected def squeeze: Int = localID

  override def autoValidate: ID = this

  override def globalize(implicit node: LocalNode): GlobalID = this
}

case class LocalID(numID: Int) extends ID {
  override def toString: String = {
    f"$numID%x"
  }

  override def shorten: String = {
    val moddedID = numID % 1000
    f"$moddedID%02x"
  }

  def squeeze: Int = numID

  def autoValidate: ID = this

  override def globalize(implicit node: LocalNode): GlobalID = GlobalID(node.info.nodeID, this.numID)
}
