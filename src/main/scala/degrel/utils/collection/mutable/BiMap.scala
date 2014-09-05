package degrel.utils.collection.mutable

import scala.collection.mutable.{HashMap => MutableHashMap, Map => MutableMap}

trait BiMap[K, V] extends MutableMap[K, V] {
  def keys: Iterable[K]

  def values: Iterable[V]

  /** @return the value corresponding to a key */
  def fromKey(k: K): Option[V]

  /** @return true if the map contains the key k */
  def containsKey(k: K) = fromKey(k).isDefined

  /** @return the key corresponding to a value */
  def fromValue(v: V): Option[K]

  /** @return true if the map contains the value v */
  def containsValue(v: V) = fromValue(v).isDefined
}

object BiMap {
}

class BiHashMap[K, V] extends BiMap[K, V] {
  private val _primary = new MutableHashMap[K, V]()
  private val _reverse = new MutableHashMap[V, K]()

  override def keys: Iterable[K] = _primary.keys

  override def values: Iterable[V] = _primary.values

  override def fromValue(v: V): Option[K] = _reverse.get(v)

  override def fromKey(k: K): Option[V] = _primary.get(k)


  override def get(key: K): Option[V] = _primary.get(key)

  override def iterator: Iterator[(K, V)] = _primary.iterator

  override def +=(kv: (K, V)): this.type = {
    _primary += kv
    _reverse += kv.swap
    this
  }

  override def -=(key: K): this.type = {
    val prevval = _primary.get(key)
    _primary -= key
    prevval match {
      case Some(v) => _reverse -= v
      case _ =>
    }
    this
  }
}
