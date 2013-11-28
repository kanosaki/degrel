package degrel.utils

import scala.collection.mutable
import java.lang.ref.{ReferenceQueue, WeakReference, Reference}

// TODO: Make threadsafe
class WeakMultiMap[K, V <: AnyRef] extends mutable.Map[K, mutable.Set[V]] with mutable.MultiMap[K, V] {
  private final val CLEANUP_RATIO = 10
  private val cleanupCounter = new CyclicCounter(CLEANUP_RATIO)
  private val _map = new mutable.HashMap[K, mutable.Set[WeakReference[V]]]()
  private val _reverseMap = new mutable.HashMap[WeakReference[V], K]()
  private val _refQueue = new ReferenceQueue[V]()

  protected def mkWeakRef(k: K, v: V) = {
    val ref = new WeakReference(v, _refQueue)
    _reverseMap += (ref -> k)
    ref
  }

  override def addBinding(k: K, v: V) = {
    if (cleanupCounter.next())
      this.removeDeadEntries()
    val wref = this.mkWeakRef(k, v)
    _map.get(k) match {
      case None => {
        val newset = new mutable.HashSet[WeakReference[V]]()
        newset += wref
        _map += (k -> newset)
      }
      case Some(set) => set += wref
    }
    this
  }

  def addBindings(kvs: Iterable[(K, V)]) = {
    for((k, v) <- kvs) {
      this.addBinding(k, v)
    }
  }

  override def entryExists(key: K, p: V => Boolean): Boolean = {
    this.get(key) match {
      case None => false
      case Some(set) => set.exists(p)
    }
  }

  override def removeBinding(key: K, value: V): this.type = {
    _map.get(key) match {
      case Some(set) => set.retain(_.get != value)
      case _ =>
    }
    this
  }

  private def retainLiveEntriesIn(vs: mutable.Set[WeakReference[V]]) = {
    vs.retain(_.get != null)
  }

  def removeDeadEntries() = {
    val keysWhichHasDeads = this.refQueueToList
      .map(_.asInstanceOf[WeakReference[V]])
      .map(_reverseMap.apply).distinct
    keysWhichHasDeads.foreach(e => this.retainLiveEntriesIn(_map.apply(e)))
  }

  private def refQueueToList = {
    var hasElement = true
    var ret: List[Reference[_ <: V]] = List()
    do {
      val elem = _refQueue.poll()
      hasElement = elem != null
      if (hasElement) {
        ret = elem :: ret
      }
    } while (hasElement)
    ret
  }

  override def +=(kv: (K, mutable.Set[V])) = {
    val key = kv._1
    val values = kv._2.map(v => {
      this.mkWeakRef(key, v)
    })
    _map += (key -> values)
    this
  }

  override def -=(key: K) = {
    _map.get(key) match {
      case Some(values) => values.foreach(_reverseMap -= _)
      case _ =>
    }
    this
  }

  override def get(key: K) = {
    _map.get(key) match {
      case Some(values) => {
        val retvalues = values.map(_.get).filter(_ != null)
        if (!retvalues.isEmpty)
          Some(retvalues)
        else
          None
      }
      case _ => None
    }
  }

  override def iterator = {
    _map.iterator.map({
                        case (k, vs) => k -> vs.map(_.get).filter(_ != null)
                      })
  }
}
