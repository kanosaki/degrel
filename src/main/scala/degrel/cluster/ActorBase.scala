package degrel.cluster

import akka.actor.Actor.Receive
import akka.actor._

trait ActorBase extends Actor with ActorLogging {
  @throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("!" * 50)
    log.error(reason, "Unhandled exception for message: {}", message)
    println("!" * 50)
    super.preRestart(reason, message)
  }

  @throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    log.warning(s"RESTARTED: reason :: ${reason.getMessage}")
    super.postRestart(reason)
  }

  def receiveBody: Receive

  def receiveUnhandled: Receive = {
    case msg => {
      log.warning(s"Unknown message: $msg")
    }
  }

  override def receive: Receive = {
    case msg => {
      try {
        receiveBody.applyOrElse(msg, receiveUnhandled)
      } catch {
        case ex: Throwable => {
          log.error(ex.toString)
          ex.printStackTrace()
          throw ex
        }
      }
    }
  }
}
