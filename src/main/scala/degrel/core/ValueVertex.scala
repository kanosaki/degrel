package degrel.core

import scala.reflect.runtime.universe.{TypeTag, typeOf}

class ValueVertex[T: TypeTag](val get: T) extends VertexBody {

  override def edges: Iterable[Edge] = Seq()

  override def attributes: Map[Label, String] = Map()

  override def shallowCopy(): Vertex = ValueVertex(this.get)

  override lazy val label: Label = this.get match {
    case str: String => Label("\"" + str + "\"")
    case other => Label(other.toString)
  }

  override def isValue: Boolean = true

  override def getValue[TV: TypeTag]: Option[TV] = {
    if (typeOf[TV] =:= typeOf[T]) {
      Some(get.asInstanceOf[TV])
    } else {
      None
    }
  }
}

object ValueVertex {
  def apply[T: TypeTag](value: T): VertexHeader = {
    VertexHeader(new ValueVertex[T](value), ID.nextLocalID())
  }
}
