package degrel.control

import java.io.File

import akka.util.Timeout
import degrel.engine.Chassis
import org.apache.commons.io.FileUtils

import scala.concurrent.duration._

class Interpreter(mainFile: File, reportStatistics: Boolean = false) {
  implicit val rewriteTimeout = Timeout(10.seconds)

  def start() = {
    val src = FileUtils.readFileToString(mainFile)
    val main = degrel.parseCell(src)
    val chassis = Chassis.create(main)
    val start = System.currentTimeMillis()
    val count = chassis.main.stepUntilStop(100000)
    val elapsed = System.currentTimeMillis() - start
    if (reportStatistics) {
      System.err.println(s"Rewrote: $count time in $elapsed ms (${count.toFloat / (elapsed.toFloat / 1000)} rps)")
    }
  }
}
