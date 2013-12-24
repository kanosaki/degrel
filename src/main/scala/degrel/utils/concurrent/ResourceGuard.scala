package degrel.utils.concurrent

import java.util.concurrent.locks.ReentrantLock

class ResourceGuard {
  private val lock = new ReentrantLock()

  def lock[V](f: => V): V = {
    lock.lock()
    try {
      f
    } finally {
      lock.unlock()
    }
  }
}
