package degrel.utils.collection.mutable

import scala.collection.mutable.{HashMap => MutableHashMap, HashSet => MutableHashSet, Map => MutableMap, MultiMap => MutableMultiMap, Set => MutableSet}
import scala.reflect.internal.util.WeakHashSet

class WeakMultiMap[K, V <: AnyRef] extends MutableMap[K, MutableSet[V]] with MutableMultiMap[K, V] {
  val inner = new MutableHashMap[K, WeakHashSet[V]]()

  override def +=(kv: (K, MutableSet[V])): WeakMultiMap.this.type = {
    inner += kv.asInstanceOf[(K, WeakHashSet[V])]
    this
  }

  override def -=(key: K): WeakMultiMap.this.type = {
    inner -= key
    this
  }


  override protected def makeSet: MutableSet[V] = {
    new WeakHashSet[V]()
  }


  override def get(key: K): Option[MutableSet[V]] = inner.get(key)

  override def iterator: Iterator[(K, MutableSet[V])] = inner.iterator
}
