package degrel.control

import degrel.engine.Chassis

import scala.concurrent.{ExecutionContext, Future}

class Interpreter(val chassis: Chassis) {
  val stepLimit = -1

  def start() = {
    this.onStarting()
    this.startProcess()
    this.onFinished()
  }

  def onStarting() = {}

  def onFinished() = {}

  def startProcess(): Future[Unit] = {
    implicit val ec = ExecutionContext.Implicits.global
    chassis.main.start() map { _ => () }
  }
}
