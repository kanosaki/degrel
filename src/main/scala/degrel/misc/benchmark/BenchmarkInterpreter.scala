package degrel.misc.benchmark

import java.time.LocalDateTime

import degrel.control.Interpreter
import degrel.core.Traverser
import degrel.engine.Chassis
import degrel.engine.sphere.QuietConsole

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

class BenchmarkInterpreter(ch: Chassis, quiet: Boolean = false) extends Interpreter(ch) {
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

  override def onFinished(): Unit = {
    this.finishTime = LocalDateTime.now()
  }
}
