package degrel.cluster

import akka.actor._

trait ActorBase extends Actor with ActorLogging {
  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("!" * 50)
    log.error(reason, "Unhandled exception for message: {}", message)
    println("!" * 50)
    super.preRestart(reason, message)
  }
}
