package degrel.utils.collection

import scala.collection.mutable.{
Map => MutableMap,
Set => MutableSet,
MultiMap => MutableMultiMap,
HashMap => MutableHashMap,
HashSet => MutableHashSet}
import java.lang.ref.{ReferenceQueue, WeakReference, Reference}
import degrel.utils.concurrent.ReadWriteGuard
import degrel.utils.CyclicCounter

// TODO: May need performance improvement.
class WeakMultiMap[K, V <: AnyRef] extends MutableMap[K, MutableSet[V]] with MutableMultiMap[K, V] {
  private final val CLEANUP_RATIO = 10
  private val lock = new ReadWriteGuard()
  private val cleanupCounter = new CyclicCounter(CLEANUP_RATIO)
  private val _map = new MutableHashMap[K, MutableSet[WeakReference[V]]]()
  private val _reverseMap = new MutableHashMap[WeakReference[V], K]()
  private val _refQueue = new ReferenceQueue[V]()

  protected def mkWeakRef(k: K, v: V) = {
    val ref = new WeakReference(v, _refQueue)
    _reverseMap += (ref -> k)
    ref
  }

  override def addBinding(k: K, v: V) =
    lock.write(
    {
      if (cleanupCounter.next())
        this.removeDeadEntries()
      val wref = this.mkWeakRef(k, v)
      _map.get(k) match {
        case None => this.makeNewEntry(k, wref)
        case Some(set) => set += wref
      }
      this
    })

  def makeNewEntry(k: K, v: WeakReference[V]) =
    lock.write(
    {
      val newset = new MutableHashSet[WeakReference[V]]()
      newset += v
      _map += (k -> newset)
    })

  def addBindings(kvs: Iterable[(K, V)]) = {
    for ((k, v) <- kvs) {
      this.addBinding(k, v)
    }
  }

  override def entryExists(key: K, p: V => Boolean): Boolean = {
    this.get(key) match {
      case None => false
      case Some(set) =>
        lock.read(
        {
          set.exists(p)
        })
    }
  }

  override def removeBinding(key: K, value: V): this.type =
    lock.read(
    {
      _map.get(key) match {
        case Some(set) => set.retain(_.get != value)
        case _ =>
      }
      this
    })

  private def retainLiveEntriesIn(vs: MutableSet[WeakReference[V]]) = {
    vs.retain(_.get != null)
  }

  def removeDeadEntries() =
    lock.write(
    {
      val keysWhichHasDeads = this.refQueueToList
        .map(_.asInstanceOf[WeakReference[V]])
        .map(_reverseMap.apply).distinct
      keysWhichHasDeads.foreach(e => this.retainLiveEntriesIn(_map.apply(e)))
    })

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

  override def +=(kv: (K, MutableSet[V])) = {
    val key = kv._1
    val values = kv._2.map(v => {
      this.mkWeakRef(key, v)
    })
    lock.write(
    {
      _map += (key -> values)
    })
    this
  }

  override def -=(key: K) =
    lock.write(
    {
      _map.get(key) match {
        case Some(values) => {
          values.foreach(_reverseMap -= _)
        }
        case _ =>
      }
      this
    })

  override def get(key: K) =
    lock.read(
    {
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
    })

  override def iterator =
    lock.read(
    {
      _map.iterator.map(
      {
        case (k, vs) => k -> vs.map(_.get).filter(_ != null)
      })
    })
}
