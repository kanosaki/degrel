package degrel.utils.signal

import java.util.concurrent._

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * スレッドプールを用いて，並列にシグナルを発行するシグナル．
 * @param blockTimeoutMillisecs 秒単位で完了待ちのタイムアウトを指定できます．0の場合は完了を待ちません．
 * @param pool
 * @tparam T
 */
class ThreadPoolSignal[T](blockTimeoutMillisecs: Long = 0)
                         (implicit pool: ExecutorService = Executors.newCachedThreadPool())
  extends Signal[T] {

  val self = this

  val handlers = new mutable.ListBuffer[Handler]()

  override def register(handler: Handler): Unit = {
    handlers += handler
  }

  override def unregister(handler: Handler): Unit = {
    handlers -= handler
  }

  override def trigger(sender: Any, obj: T): Unit = {
    val frozenHandlers = Seq(handlers: _*)
    val cdLatch = new CountDownLatch(frozenHandlers.size)
    val handlerCallables: Seq[Callable[Unit]] = frozenHandlers.map(h => new Callable[Unit] {
      override def call(): Unit = {
        h(sender, obj)
        cdLatch.countDown()
      }
    })
    pool.invokeAll(handlerCallables.asJavaCollection)
    if (blockTimeoutMillisecs > 0) {
      cdLatch.await(blockTimeoutMillisecs, TimeUnit.MILLISECONDS)
    }
  }

}
