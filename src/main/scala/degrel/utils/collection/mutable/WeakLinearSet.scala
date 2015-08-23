package degrel.utils.collection.mutable

import java.lang.ref.{ReferenceQueue, WeakReference}

import scala.collection.mutable

class WeakLinearSet[T] extends mutable.Set[T] {
  private[this] var items = mutable.ListBuffer[WeakReference[T]]()
  private[this] val queue = new ReferenceQueue[T]()


  override def +=(elem: T): WeakLinearSet.this.type = {
    this.removeStaleEntries()
    val r = new WeakReference(elem, queue)
    items += r
    this
  }

  def removeStaleEntries(): Unit = {
    val item = queue.poll().asInstanceOf[WeakReference[T]]
    if (item != null) {
      items -= item
      removeStaleEntries()
    }
  }

  override def -=(elem: T): WeakLinearSet.this.type = {
    this.removeStaleEntries()
    items = items.filter { item =>
      val v = item.get
      v != null && v != elem
    }
    this
  }

  override def contains(elem: T): Boolean = {
    this.removeStaleEntries()
    items.exists(_.get == elem)
  }

  override def iterator: Iterator[T] = {
    this.removeStaleEntries()
    items.iterator.map(_.get).filter(_ != null)
  }
}

object WeakLinearSet {
  def apply[T]() = {
    new WeakLinearSet[T]()
  }
}
