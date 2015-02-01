package degrel.utils.concurrent

import java.util.concurrent.locks.ReentrantReadWriteLock

class ReadWriteGuard {
  private[this] val lock = new ReentrantReadWriteLock()

  def read[V](f: => V): V = {
    lock.readLock().lock()
    try {
      f
    } finally {
      lock.readLock().unlock()
    }
  }

  def write[V](f: => V): V = {
    lock.writeLock().lock()
    try {
      f
    } finally {
      lock.writeLock().unlock()
    }
  }
}
