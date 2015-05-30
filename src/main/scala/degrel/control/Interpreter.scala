package degrel.control

import java.io.File

import degrel.engine.Chassis
import org.apache.commons.io.FileUtils

class Interpreter(mainFile: File) {
  val stepLimit = -1
  var chassis: Chassis = {
    val src = FileUtils.readFileToString(mainFile)
    val main = degrel.parseCell(src)
    Chassis.create(main)
  }

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
