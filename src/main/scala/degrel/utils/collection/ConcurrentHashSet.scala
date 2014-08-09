package degrel.utils.collection

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.{Set => MutableSet}
import scala.collection.JavaConversions._

class ConcurrentHashSet[T] extends MutableSet[T] {
  val internalMap = new ConcurrentHashMap[T, Unit]()

  def +=(elem: T): this.type = {
    internalMap.put(elem, ())
    this
  }

  def -=(elem: T): this.type = {
    internalMap.remove(elem)
    this
  }

  def contains(elem: T): Boolean = internalMap.containsKey(elem)

  def iterator: Iterator[T] = internalMap.keys()
}
