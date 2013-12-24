package degrel.utils.collection

class LookaheadIterator[T](source: Iterator[T]) extends BufferedIterator[T] {
  private var ahead: Option[T] = None
  this.bufferNext()

  private def bufferNext() = {
    if(source.hasNext)
      this.ahead = Some(source.next())
  }

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

  def head: T = this.ahead.get
}
