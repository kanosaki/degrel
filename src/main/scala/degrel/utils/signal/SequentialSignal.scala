package degrel.utils.signal

import scala.collection.mutable

class SequentialSignal[T] extends Signal[T] {
  val handlers = new mutable.ListBuffer[Handler]()

  override def register(handler: Handler): Unit = {
    handlers += handler
  }

  override def unregister(handler: Handler): Unit = {
    handlers -= handler
  }

  override def trigger(sender: Any, obj: T): Unit = {
    for (h <- handlers) {
      h(sender, obj)
    }
  }

}
