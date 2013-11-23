package degrel.core


trait ID {

}

object ID {
}



case class LocalID(id : Int) extends ID{
  override  def toString: String = {
    f"$id%x"
  }
}
