package degrel.core

import scala.collection.mutable

class Footprints[E] extends Trajectory {
  private lazy val footprints = new mutable.HashMap[Vertex, E]()

  def resultOf(v: Vertex) = {
    footprints.get(v) match {
      case Some(e) => e
      case None => throw new Exception(s"The vertex $v haven't been traversed previously.")
    }
  }

  def stamp(v: Vertex)(f: PartialFunction[Either[this.type, this.type], E]): E = {
    val result = super.walk(v)(f)
    footprints += v -> result
    result
  }
}
