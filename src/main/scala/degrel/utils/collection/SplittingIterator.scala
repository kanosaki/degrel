package degrel.utils.collection

class SplittingIterator[T](source: Iterator[T])
                          (implicit comp: (T, T) => Boolean)
  extends Iterator[Iterable[T]] {
  private var peekedItem: Option[T] = None

  override def hasNext: Boolean = peekedItem.isDefined || source.hasNext

  override def next(): Iterable[T] = {
    if (!source.hasNext) {
      val ret = Seq(peekedItem.get)
      peekedItem = None
      return ret
    }
    val first = source.next()
    peekedItem match {
      case Some(prev) => {
        if (comp(prev, first)) {
          val following = takeWhile(source, comp(first, _))
          Seq(prev, first) ++ following
        } else {
          peekedItem = Some(first)
          Seq(prev)
        }
      }
      case None => {
        val following = takeWhile(source, comp(first, _))
        Seq(first) ++ following
      }
    }
  }

  def takeWhile(src: Iterator[T], pred: T => Boolean): List[T] = {
    if (src.hasNext) {
      val nxt = src.next()
      if (pred(nxt)) {
        nxt :: takeWhile(src, pred)
      } else {
        peekedItem = Some(nxt)
        Nil
      }
    } else {
      Nil
    }
  }
}
