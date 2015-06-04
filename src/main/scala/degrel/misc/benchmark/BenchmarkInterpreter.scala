package degrel.misc.benchmark

import java.io.File
import java.time.LocalDateTime

import degrel.control.Interpreter
import degrel.core.Traverser
import degrel.engine.sphere.QuietConsole

class BenchmarkInterpreter(f: File, quiet: Boolean = false) extends Interpreter(f) {
  var startTime: LocalDateTime = null
  var finishTime: LocalDateTime = null
  val initialSize = Traverser(chassis.main.header).size
  var elapsed: Long = -1
  var totalSteps: Long = -1

  override def onStarting(): Unit = {
    // Disable output
    if (quiet) {
      chassis.sphere.console = new QuietConsole()
    }
    this.startTime = LocalDateTime.now()
  }

  override def startProcess(): Long = {
    this.totalSteps = super.startProcess()
    this.totalSteps
  }

  override def onFinished(): Unit = {
    this.finishTime = LocalDateTime.now()
  }
}
