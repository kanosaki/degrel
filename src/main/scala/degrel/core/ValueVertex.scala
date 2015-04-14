package degrel.core

trait ValueVertex[T] extends VertexBody {
  val get: T

  override def edges: Iterable[Edge] = Seq()

  override def attributes: Map[Label, String] = Map()

  override def shallowCopy(): Vertex = ValueVertex(this.get)

  override lazy val label: Label = this.get match {
    case str: String => Label("\"" + str + "\"")
    case other => Label(other.toString)
  }

  override def isValue[TV]: Boolean = {
    get.isInstanceOf[TV]
  }

  override def getValue[TV]: Option[TV] = {
    if (this.isValue[TV]) {
      Some(get.asInstanceOf[TV])
    } else {
      None
    }
  }
}

class ObjectVertex[T](val get: T) extends ValueVertex[T] {

}

object ValueVertex {
  def apply[T](value: T): ValueVertex[T] = {
    new ObjectVertex[T](value)
  }
}
