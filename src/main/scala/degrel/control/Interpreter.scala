package degrel.control

import degrel.engine.Chassis

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

class Interpreter(val chassis: Chassis) {
  val stepLimit = -1
  private implicit val ec = ExecutionContext.Implicits.global

  def start(): Future[Unit] = async {
    this.onStarting()
    await(this.startProcess())
    this.onFinished()
  }

  def onStarting() = {}

  def onFinished() = {}

  def startProcess(): Future[Unit] = {
    chassis.main.start() map { _ => () }
  }
}
