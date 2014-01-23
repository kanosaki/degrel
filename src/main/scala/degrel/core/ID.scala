package degrel.core


trait ID extends Comparable[ID] {
  def shorten: String = {
    this.toString.substring(0, 3)
  }

  protected def squeeze: Int

  def compareTo(o: ID) = {
    this.squeeze - o.squeeze
  }

  def autoValidate: ID
}

object ID {
  private var idCounter: Int = -1

  private def nextID: ID = {
    idCounter += 1
    LocalID(idCounter)
  }

  def NA: ID = NotAssignedID

  def autoAssign: ID = this.nextID
}

case object NotAssignedID extends ID {
  protected def squeeze: Int = throw new Exception("Couldnt compare NotAssignedID")

  def autoValidate: ID = ID.autoAssign
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
}
