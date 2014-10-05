package degrel.utils.collection

/**
 * Iterator[T]に対するBufferedIterator[T]の実装．
 * {@code next()}が呼ばれた時点で，そのとき返された次の要素を{@code head}へ保持します
 * @param source 元のIterator[T]
 * @tparam T Iterator[T]の要素の型
 */
class LookaheadIterator[T](source: Iterator[T]) extends BufferedIterator[T] {
  private var ahead: Option[T] = None
  this.bufferNext()

  def hasNext: Boolean = {
    (this.ahead, source.hasNext) match {
      case (None, false) => false
      case _ => true
    }
  }

  def next(): T = {
    val nextobj = this.head
    this.bufferNext()
    nextobj
  }

  private def bufferNext() = {
    if (source.hasNext)
      this.ahead = Some(source.next())
  }

  def head: T = this.ahead.get
}
