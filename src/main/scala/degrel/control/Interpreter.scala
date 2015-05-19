package degrel.control

import java.io.File

import akka.util.Timeout
import degrel.engine.Chassis
import org.apache.commons.io.FileUtils

import scala.concurrent.duration._

class Interpreter(mainFile: File) {
  implicit val rewriteTimeout = Timeout(10.seconds)

  def start() = {
    val src = FileUtils.readFileToString(mainFile)
    val main = degrel.parseCell(src)
    val chassis = Chassis.create(main)
    chassis.main.stepUntilStop(1000)
  }
}
