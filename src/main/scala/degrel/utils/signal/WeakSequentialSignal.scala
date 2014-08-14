package degrel.utils.signal

import scala.collection.mutable
import scala.ref.WeakReference
import degrel.utils.concurrent.ReadWriteGuard

class WeakSequentialSignal[T] extends Signal[T] {
  private val handlers = new mutable.ListBuffer[WeakReference[Handler]]
  private val guard = new ReadWriteGuard()

  override def register(handler: Handler): Unit = guard.write(
  {
    val href = new WeakReference[Handler](handler)
    handlers += href
  })

  override def unregister(handler: Handler): Unit = guard.write(
  {
    handlers
      .filter(_.get == Some(handler))
      .foreach(handlers -= _)
  })

  override def trigger(sender: Any, obj: T): Unit = {
    val expiredHandlers = new mutable.ListBuffer[WeakReference[Handler]]()
    for (h <- handlers) {
      h.get match {
        case Some(realHandler) => realHandler(sender, obj)
        case None => expiredHandlers -= h
      }
    }
    if (expiredHandlers.nonEmpty)
      guard.write(
      {
        expiredHandlers.foreach(handlers -= _)
      })
  }
}
