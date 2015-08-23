package degrel.control

import degrel.engine.Chassis

class Interpreter(val chassis: Chassis) {
  val stepLimit = -1

  def start() = {
    this.onStarting()
    this.startProcess()
    this.onFinished()
  }

  def onStarting() = {}

  def onFinished() = {}

  def startProcess(): Long = {
    chassis.main.stepUntilStop(stepLimit)
  }
}
