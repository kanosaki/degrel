package degrel.utils.collection

class LoopIterator[T](val source: Iterable[T]) extends Iterator[T] {
  protected var currentItor = source.iterator
  protected var sourceIsEmpty = source.isEmpty

  private def checkIterator(): Unit = {
    if(!currentItor.hasNext) {
      currentItor = source.iterator
      sourceIsEmpty = source.isEmpty
    }
  }

  override def hasNext: Boolean = {
    this.checkIterator()
    !sourceIsEmpty
  }

  override def next(): T = {
    currentItor.next()
  }
}

object LoopIterator {
  def apply[T](src: Iterable[T]): LoopIterator[T] = {
    new LoopIterator[T](src)
  }
}
