package degrel.face

import degrel.core.{ID, Vertex}

import scala.collection.mutable

class FaceRepository extends mutable.Map[ID, Vertex] {

  private val map = new mutable.HashMap[ID, Vertex]()

  def expose(v: Vertex) = {
    map += v.id -> v
  }

  override def +=(kv: (ID, Vertex)) = {
    map += kv
    this
  }

  override def -=(key: ID) = {
    throw new UnsupportedOperationException()
  }

  override def get(key: ID): Option[Vertex] = map.get(key)

  override def iterator: Iterator[(ID, Vertex)] = map.iterator
}
