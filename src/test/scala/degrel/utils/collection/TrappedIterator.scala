package degrel.utils.collection

class TrappedIterator[A](inner: Iterator[A])(trapPredicate: (A, Int) => Boolean) extends Iterator[A] {
  var counter = 0

  override def next(): A = {
    val nextItem = inner.next()
    if (trapPredicate(nextItem, counter)) {
      throw new IterationTrappedException("Iteration trapped.", inner)
    }
    counter += 1
    nextItem
  }

  override def hasNext: Boolean = inner.hasNext
}

class IterationTrappedException[A](msg: String, trapped: Iterator[A]) extends Exception(msg) {

}
