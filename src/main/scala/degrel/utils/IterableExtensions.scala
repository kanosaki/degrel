package degrel.utils

import scala.language.implicitConversions
import scala.collection.mutable.ListBuffer
import degrel.utils.collection.LookaheadIterator

object IterableExtensions {
  class IterableExtensions[T](it: Iterator[T]) {

    def mapUntil[V](f: T => Option[V]): Iterator[V] = {
      class InnerIterator extends Iterator[V] {
        val srcBuf = new LookaheadIterator(it.map(f))

        def hasNext: Boolean = {
          if(!srcBuf.hasNext) return false
          srcBuf.head match {
            case Some(_) => true
            case None => false
          }
        }

        def next(): V = {
          srcBuf.next() match {
            case Some(v) => v
            case None => throw new IllegalStateException()
          }
        }
      }
      new InnerIterator()
    }

    def mapFilter[V](f: T => Option[V]): Iterator[V] = {
      it.map(f).filter(_.isDefined).map(_.get)
    }

    def mapAllOrNone[V](f: T => Option[V]): Option[Seq[V]] = {
      val ret = new ListBuffer[V]
      var failed = false
      while(!failed && it.hasNext) {
        f(it.next()) match {
          case Some(v) => ret += v
          case None => failed = true
        }
      }
      if(!failed)
        Some(ret.toList)
      else
        None
    }
  }

  implicit def iterableExtensions[T](it: Iterable[T]) = new IterableExtensions(it.iterator)

  implicit def iteratorExtensions[T](it: Iterator[T]) = new IterableExtensions(it)
}
