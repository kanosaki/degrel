package degrel.utils.collection

import degrel.utils.concurrent.ReadWriteGuard

import scala.collection.mutable.{PriorityQueue => MutablePriorityQueue}
import scala.util.Random

class ShuffledIterator[A](inner: Iterator[A], val bufferSize: Int) extends Iterator[A] {

  type E = ItemWrapper[A]

  private val buffer = new MutablePriorityQueue[E]()(ItemComparator)
  private val bufferLock = new ReadWriteGuard()
  private val random = new Random()

  this.fillBuffer()

  override def next(): A = {
    val ret = bufferLock.write {
      buffer.dequeue().item
    }
    this.fillBuffer()
    ret
  }

  def fillBuffer() = {
    bufferLock.write {
      while (buffer.size < bufferSize && inner.hasNext) {
        this.insertItem(inner.next())
      }
    }
  }

  def insertItem(item: A) = {
    bufferLock.write {
      buffer.enqueue(new ItemWrapper(item))
    }
  }

  override def hasNext: Boolean = {
    bufferLock.read {
      !buffer.isEmpty
    }
  }

  class ItemWrapper[A](val item: A) {
    val rand: Float = random.nextFloat()
  }

  object ItemComparator extends Ordering[E] {
    override def compare(x: E, y: E): Int = {
      x.rand.compare(y.rand)
    }
  }

}

object ShuffledIterator {
  def apply[A](it: Iterator[A], bufferSize: Int = 100): ShuffledIterator[A] = {
    new ShuffledIterator[A](it, bufferSize)
  }
}
