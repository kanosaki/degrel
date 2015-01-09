package degrel.utils.collection

import degrel.utils.concurrent.ReadWriteGuard

import scala.collection.mutable.{PriorityQueue => MutablePriorityQueue}
import scala.util.Random

/**
 * イテレータをシャッフルします．シャッフルは擬似的な物で，
 * イテレータから{@code bufferSize}の要素を取り出し，シャッフルしてからバッファリングします．
 * バッファが空になると，また元のイテレータから{@code bufferSize}の取り出しを行います
 * @param inner 元となるIterator[T]
 * @param bufferSize シャッフルのためにバッファリングするサイズ
 * @tparam A Iterator[T]の要素の型
 */
class ShuffledIterator[A](inner: Iterator[A], val bufferSize: Int) extends Iterator[A] {
  type E = ItemWrapper

  private val buffer = new MutablePriorityQueue[E]()
  private val bufferLock = new ReadWriteGuard()
  private val random = new Random()

  this.fillBuffer()

  override def next(): A = {
    val ret = bufferLock.write {
      buffer.dequeue().item
    }
    if (buffer.isEmpty) {
      this.fillBuffer()
    }
    ret
  }

  /**
   * バッファを満タンまで満たします
   */
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
      buffer.nonEmpty
    }
  }

  /**
   * PriorityQueueへランダムな優先順位を提供するためのラッパー
   * @param item
   */
  class ItemWrapper(val item: A) extends Ordered[E] {
    val rand: Float = random.nextFloat()

    override def compare(that: E): Int = {
      this.rand.compareTo(that.rand)
    }
  }

}

object ShuffledIterator {
  def apply[A](it: Iterator[A], bufferSize: Int = 100): ShuffledIterator[A] = {
    new ShuffledIterator[A](it, bufferSize)
  }
}
