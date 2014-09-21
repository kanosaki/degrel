package degrel.utils.collection.mutable

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions._
import scala.collection.mutable.{Set => MutableSet}

object ConcurrentHashSet {
  private val DUMMY_OBJ = new Object()
}

class ConcurrentHashSet[T] extends MutableSet[T] {
  val internalMap = new ConcurrentHashMap[T, Object]()

  def +=(elem: T): this.type = {
    internalMap.put(elem, ConcurrentHashSet.DUMMY_OBJ)
    this
  }

  def -=(elem: T): this.type = {
    internalMap.remove(elem)
    this
  }

  /**
   * 要素が存在しない場合は追加してtrueを，存在する場合は何もせずfalseを返します
   * @param elem
   * @return
   */
  def putIfAbsent(elem: T): Boolean = {
    internalMap.putIfAbsent(elem, ConcurrentHashSet.DUMMY_OBJ) == null
  }

  def contains(elem: T): Boolean = internalMap.containsKey(elem)

  def iterator: Iterator[T] = internalMap.keys()

}
