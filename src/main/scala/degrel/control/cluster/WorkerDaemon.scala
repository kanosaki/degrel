package degrel.control.cluster

class WorkerDaemon(facade: WorkerFacade) {
  def start() = {
    facade.start()
  }
}

object WorkerDaemon {
  def apply(facade: WorkerFacade) = {
    new WorkerDaemon(facade)
  }
}
