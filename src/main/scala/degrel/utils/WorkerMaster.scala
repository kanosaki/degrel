package degrel.utils

import java.util.concurrent._
import degrel.utils.collection.ConcurrentHashSet
import degrel.utils.concurrent.ResourceGuard

class WorkerMaster(threadNum: Int = -1) {
  val threadNumber = if (threadNum > 0) threadNum else {Runtime.getRuntime.availableProcessors()}

  val executor = Executors.newFixedThreadPool(threadNumber).asInstanceOf[ThreadPoolExecutor]

  val queued = new LinkedBlockingQueue[WorkerTask]()

  val working = new ConcurrentHashSet[WorkerTask]()

  val stopped = new ConcurrentLinkedDeque[WorkerTask]()

  val modifying = new ResourceGuard()


  private def requeueWorkers() = {
    queued.addAll(stopped)
    stopped.clear()
  }


  def start() = {
    implicit val master = this
    do {
      val next = queued.poll(100, TimeUnit.MILLISECONDS)
      if (next != null) {
        working += next
        executor.submit(runnable(next.start))
      }
    } while (!this.isStopped)
  }

  def onTaskComplete(task: WorkerTask, result: TaskResult) = {
    result.succeed match {
      case true => this.onTaskSucceed(task, result)
      case false => this.onTaskFailure(task, result)
    }
  }

  def onTaskFailure(task: WorkerTask, result: TaskResult) = {
  }

  def onTaskSucceed(task: WorkerTask, result: TaskResult) = {
    if (result.shouldResetTasks) {
      modifying.lock {
        queued.put(task)
        working -= task
        this.requeueWorkers()
      }
    } else {
      modifying.lock {
        working -= task
        stopped.add(task)
      }
    }
  }

  def onTaskException(task: WorkerTask, e: Exception) = {
    throw e
  }

  def isStopped: Boolean = modifying.lock {queued.isEmpty && working.isEmpty}
}

trait WorkerTask {
  def start()(implicit master: WorkerMaster): Unit = {
    try {
      val result = this.run()
      master.onTaskComplete(this, result)
    } catch {
      case e: Exception => master.onTaskException(this, e)
    }
  }

  def run(): TaskResult

}

trait TaskResult {
  def shouldResetTasks: Boolean = false

  def succeed: Boolean
}
